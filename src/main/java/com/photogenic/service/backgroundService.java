package com.photogenic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;

@Service
public class backgroundService {

    @Value("${background.removal.api.key}")
    private String apiKey;

    @Value("${background.removal.api.url}")
    private String apiUrl;
    private final RestTemplate restTemplate;
    public backgroundService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${background.removal.api.connect-timeout:5000}") Duration connectTimeout,
            @Value("${background.removal.api.read-timeout:10000}") Duration readTimeout) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)     // Set read timeout using injected value
                .build();
    }

    public ResponseEntity<byte[]> removeBackground(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Input file cannot be null or empty.");
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("X-Api-Key", apiKey);

        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        String requestJson = String.format("{\"image_file_b64\":\"%s\", \"size\":\"auto\"}", base64Image);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, requestHeaders);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                byte[].class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            String errorMessage = "Error from background removal API.";
            if (response.hasBody() && response.getBody() != null) {
                errorMessage += " Details: " + new String(response.getBody());
            }
            throw new IOException(errorMessage);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        if (response.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE)) {
            responseHeaders.setContentType(response.getHeaders().getContentType());
        }
        return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
    }
}