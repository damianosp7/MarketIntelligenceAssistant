package gr.excersice.watsonxai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

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



    public String fetchGoogleSearchOutput(String input) {
        setBearerToken();
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + bearerToken);

            String requestBody = "{\"tool_name\": \"GoogleSearch\", \"input\": \"" + input + "\", \"config\": {\"maxResults\": 0}}";

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(urlDataPlatform+agentsRunEndpoint, HttpMethod.POST, entity, String.class);
            JSONObject jsonResponse = new JSONObject(response.getBody());

            return jsonResponse.toString().substring(11, jsonResponse.toString().length()-2);
//            return jsonResponse.getString("output");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Google search output: " + e.getMessage());
        }
    }

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
        ResponseEntity<String> genResponse = restTemplate.exchange(ibmUrlDallasML+ibmTextGenerationEndpoint, HttpMethod.POST, genEntity, String.class);
        JSONObject jsonResponse = new JSONObject(genResponse.getBody());
        JSONArray results = jsonResponse.getJSONArray("results");
        return results.getJSONObject(0).getString("generated_text");
    }

    public void setBearerToken() {
        this.bearerToken = tokenService.fetchAccessToken();
    }

    public byte[] generatePdfFromString(String content) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                float margin = 50;
                float yPosition = page.getMediaBox().getHeight() - margin;
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

            File file = new File("analysis.pdf");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(outputStream.toByteArray());
            }

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
        }
    }

    private String[] wrapText(String text, float maxWidth) {
        float fontSize = 12;
        float charWidth = fontSize * 0.5f;
        int maxCharsPerLine = (int) (maxWidth / charWidth);
        return text.replaceAll("(.{" + maxCharsPerLine + "})", "$1\n").split("\n");
    }


}
