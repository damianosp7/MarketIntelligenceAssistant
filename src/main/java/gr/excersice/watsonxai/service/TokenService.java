package gr.excersice.watsonxai.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TokenService {

    @Value("${ibm.apikey}")
    private String apikey;

    @Value("${ibm.bearerToken}")
    private String bearerToken;

    @Value("${ibm.url.dallas}")
    private String urlDallas;

    @Value("${ibm.url.token}")
    private String tokenEndpoint;

    /**
     * Fetches the access token from the IBM Cloud IAM service
     * @return the access token
     */
    public String fetchAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        String url = urlDallas + tokenEndpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=urn:ibm:params:oauth:grant-type:apikey&apikey=" + apikey;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            JSONObject jsonResponse = new JSONObject(response.getBody());
            setBearerToken(jsonResponse.getString("access_token"));
            return jsonResponse.getString("access_token");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching token: " + e.getMessage());
        }
    }

    /**
     * Sets the bearer token
     * @return the bearer token
     */
    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

}