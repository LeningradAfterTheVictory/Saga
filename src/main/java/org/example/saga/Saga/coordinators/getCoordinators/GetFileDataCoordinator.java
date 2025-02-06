package org.example.saga.Saga.coordinators.getCoordinators;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

public class GetFileDataCoordinator {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${base.attraction.url}")
    private String baseAttractionUrl;

    @Value("${base.s3.url}")
    private String baseS3Url;

    public void tryGetFileData(String fileUrl) {
        byte[] content = downloadFileContentFromS3(fileUrl);
    }

    private byte[] downloadFileContentFromS3(String fileUrl) {
        try {
            var response = restTemplate.getForEntity(baseS3Url + fileUrl, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                System.out.println("Error: " + response.getStatusCode());
                return new byte[0];
            }
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
        }
        return new byte[0];
    }
}
