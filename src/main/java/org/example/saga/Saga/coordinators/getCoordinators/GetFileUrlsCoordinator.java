package org.example.saga.Saga.coordinators.getCoordinators;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GetFileUrlsCoordinator {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${base.attraction.url}")
    private String baseAttractionUrl;

    @Value("${base.s3.url}")
    private String baseS3Url;

    public void tryGetFileUrls(String folder) {
        List<String> content = downloadAllFileUrlsFromS3(folder);
    }

    private List<String> downloadAllFileUrlsFromS3(String folder) {
        try {
            var response = restTemplate.getForEntity(baseS3Url + folder, List.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                System.out.println("Error: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
        }
        return null;
    }
}
