package com.photogenic.service;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import java.io.IOException;
import java.util.Base64;
import com.photogenic.config.pgConfigRest;

@Service
public class backgroundService {
    private static final String API_KEY = "8cdvrTCcs7kU3pVNuK8457TA";
    private static final String API_URL = "https://api.remove.bg/v1.0/removebg";

    @Autowired
    private pgConfigRest Rest;

    public byte[] removeBackground(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", API_KEY);

        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        String requestJson = String.format("{\"image_file_b64\":\"%s\", \"size\":\"auto\"}", base64Image);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<byte[]> response = Rest.restTemplate().exchange(
                API_URL,
                HttpMethod.POST,
                entity,
                byte[].class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new IOException("Error: " + response.getStatusCode() + " - " + new String(response.getBody()));
        }
    }
}
