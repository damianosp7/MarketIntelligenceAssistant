package gr.excersice.watsonxai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.excersice.watsonxai.constants.Constants;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Service class that provides various implementations for generating PDFs, fetching Google search output,
 * generating analysis based on search results, and handling text formatting.
 */
@Service
public class ImplementationService {

    @Value("${ibm.bearerToken}")
    private String bearerToken;

    @Value("${ibm.url.dataplatform}")
    private String urlDataPlatform;

    @Value("${ibm.url.agents.run}")
    private String agentsRunEndpoint;

    @Value("${ibm.projectId}")
    private String projectID;

    @Value("${ibm.url.dallas.ml}")
    private String ibmUrlDallasML;

    @Value("${ibm.url.text.generation}")
    private String ibmTextGenerationEndpoint;

    @Autowired
    private TokenService tokenService;

    /**
     * Sets the bearer token by fetching a new access token from the TokenService.
     */
    public void setBearerToken() {
        this.bearerToken = tokenService.fetchAccessToken();
    }

    /**
     * Fetches the Google search output using the provided input string.
     * The method sets the bearer token, constructs the request, and sends it to the specified endpoint.
     *
     * @param input the input string for the Google search
     * @return the JSON response as a string, excluding the first 11 and last 2 characters
     * @throws RuntimeException if an error occurs while fetching the Google search output
     */
    public String fetchGoogleSearchOutput(String input) {
        setBearerToken();
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + bearerToken);

            String requestBody = "{\"tool_name\": \"GoogleSearch\", \"input\": \"" + input + "\", \"config\": {\"maxResults\": 0}}";

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(urlDataPlatform + agentsRunEndpoint, HttpMethod.POST, entity, String.class);
            JSONObject jsonResponse = new JSONObject(response.getBody());

            return jsonResponse.toString().substring(11, jsonResponse.toString().length() - 2);
//            return jsonResponse.getString("output");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Google search output: " + e.getMessage());
        }
    }

    /**
     * Generates an analysis based on the Google search output for the given input string.
     * The method fetches the Google search output, constructs a request to the IBM text generation endpoint,
     * and returns the generated analysis text.
     *
     * @param input the input string for the Google search and analysis
     * @return the generated analysis text
     * @throws JsonProcessingException if an error occurs while processing JSON
     */
    public String generateAnalysisBasedOnSearch(String input) throws JsonProcessingException {
        String searchOutput = fetchGoogleSearchOutput(input);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + bearerToken);
        String genRequestBody = "{"
                + "\"model_id\": \"" + Constants.modelId + "\", "
                + "\"input\": \"" + Constants.agentPrompt
                + searchOutput
                + Constants.agentPromptWords + "\", "
                + "\"parameters\": {"
                + "\"decoding_method\": \"" + Constants.decodingMethodGreedy + "\", "
                + "\"max_new_tokens\": " + Constants.maxNewTokens + ", "
                + "\"min_new_tokens\": 0, "
                + "\"stop_sequences\": [], "
                + "\"repetition_penalty\": 1"
                + "}, "
                + "\"project_id\": \"" + projectID + "\""
                + "}";
        HttpEntity<String> genEntity = new HttpEntity<>(genRequestBody, headers);
        ResponseEntity<String> genResponse = restTemplate.exchange(ibmUrlDallasML + ibmTextGenerationEndpoint, HttpMethod.POST, genEntity, String.class);
        JSONObject jsonResponse = new JSONObject(genResponse.getBody());
        JSONArray results = jsonResponse.getJSONArray("results");
        return results.getJSONObject(0).getString("generated_text");
    }


    /**
     * Generates a PDF from the given content and input string.
     * The PDF includes a title derived from the input string and the content wrapped to fit within the page margins.
     *
     * @param content the content to be included in the PDF
     * @param input the input string used to generate the title of the PDF
     * @return a byte array representing the generated PDF
     * @throws RuntimeException if an error occurs during PDF generation
     */
    public byte[] generatePdfFromString(String content,String input) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                // Set font for the title
                PDType1Font titleFont = PDType1Font.HELVETICA_BOLD;
                float fontSize = 18;
                contentStream.setFont(titleFont, fontSize);

                // Title text
                String title = capitalizeWords(input) + " Analysis";

                // Calculate title width to center it
                float titleWidth = titleFont.getStringWidth(title) * fontSize / 1000;  // Correct string width calculation
                float xPosition = (page.getMediaBox().getWidth() - titleWidth) / 2;
                float yPosition = page.getMediaBox().getHeight() - 100; // 100 units from top of page

                // Add title to the center of the first page
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition, yPosition);
                contentStream.showText(title);
                contentStream.endText();

                // Set font for the body text
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                float margin = 50;
                yPosition = page.getMediaBox().getHeight() - 130;
                float lineHeight = 15;
                float maxWidth = page.getMediaBox().getWidth() - 2 * margin;

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition); // Initial cursor position

                for (String line : content.split("\n")) {
                    for (String wrappedLine : wrapText(line, maxWidth)) {
                        if (yPosition <= margin) {
                            contentStream.endText();
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            contentStream.setFont(PDType1Font.HELVETICA, 12);
                            yPosition = page.getMediaBox().getHeight() - margin;
                            contentStream.beginText();
                            contentStream.newLineAtOffset(margin, yPosition);
                        }
                        contentStream.showText(wrappedLine);
                        yPosition -= lineHeight;
                        contentStream.newLineAtOffset(0, -lineHeight); // Move cursor for next line
                    }
                }
                contentStream.endText();
                contentStream.close();
            } catch (Exception e) {
                contentStream.close();
                throw new RuntimeException("Error writing to PDF: " + e.getMessage());
            }

            document.save(outputStream);
            String fileName = capitalizeWords(input).replaceAll("\\s+", "") + "Analysis.pdf";
            File file = new File(fileName.trim());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(outputStream.toByteArray());
            }

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
        }
    }

    /**
     * Wraps the input text to fit within the specified maximum width.
     * The text is split into lines such that each line does not exceed the maximum width.
     *
     * @param text the input text to be wrapped
     * @param maxWidth the maximum width for each line
     * @return an array of strings, where each string is a line of wrapped text
     */
    private String[] wrapText(String text, float maxWidth) {
        float fontSize = 12;
        float charWidth = fontSize * 0.5f;
        int maxCharsPerLine = (int) (maxWidth / charWidth);
        return text.replaceAll("(.{" + maxCharsPerLine + "})", "$1\n").split("\n");
    }

    /**
     * Capitalizes the first letter of each word in the input string and converts the rest of the letters to lowercase.
     *
     * @param input the input string containing words to be capitalized
     * @return a string with the first letter of each word capitalized and the rest in lowercase,
     *         or the original string if it is null or empty
     */
    public static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return capitalized.toString().trim();
    }


}
