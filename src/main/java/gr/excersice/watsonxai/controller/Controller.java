package gr.excersice.watsonxai.controller;

import gr.excersice.watsonxai.service.ImplementationService;
import gr.excersice.watsonxai.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class Controller {

    public static String analysis;
    public static String inputString;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ImplementationService implementationService;

    @PostMapping("/token")
    public String getAccessToken() {
        try {
            return tokenService.fetchAccessToken();
        } catch (Exception e) {
            return new String("Error: " + e.getMessage() + HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/google")
    public String getGoogleSearch(@RequestParam String input) {
        try {
            return implementationService.fetchGoogleSearchOutput(input);
        } catch (Exception e) {
            return new String("Error: " + e.getMessage() + HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/analyze")
    public String getGeneratedAnalysis(@RequestParam String input) {
        try {
             analysis = implementationService.generateAnalysisBasedOnSearch(input);
            inputString = input;
            return implementationService.generateAnalysisBasedOnSearch(input);
        } catch (Exception e) {
            return new String("Error: " + e.getMessage() + HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadPdf()  {
        byte[] pdfBytes = implementationService.generatePdfFromString(analysis, inputString);
        if (pdfBytes == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=download.pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }


}