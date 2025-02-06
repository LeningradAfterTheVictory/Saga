package org.example.saga.Saga.coordinators.getCoordinators;

import org.example.saga.Saga.dto.AttractionSummaryDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class GetAllAttractionsCoordinator {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${base.attraction.url}")
    private String baseAttractionUrl;

    @Value("${base.s3.url}")
    private String baseS3Url;

    public void tryGetAllAttractions(String fileUrl) {
        List<AttractionSummaryDTO> content = getAttractionsFromS3(fileUrl);
    }

    private List<AttractionSummaryDTO> getAttractionsFromS3(String fileUrl) {
        try {
            var response = restTemplate.getForEntity(baseAttractionUrl + "/get-all", List.class);

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
