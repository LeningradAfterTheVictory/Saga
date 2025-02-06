package org.example.saga.Saga.coordinators.uploadCoordinators;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class UploadFileCoordinator {

    private final RestTemplate restTemplate;
    private String uploadedFileUrl = "";

    @Value("${base.attraction.url}")
    private String baseAttractionUrl;

    @Value("${base.s3.url}")
    private String baseS3Url;

    public UploadFileCoordinator() {
        this.restTemplate = new RestTemplate();
    }

    public void tryUpload(MultipartFile file) {
        tryExtractingViaS3(baseS3Url, file);
        tryUploadToDB(baseAttractionUrl, baseS3Url);
    }

    public void tryExtractingViaS3(String baseS3Url, MultipartFile file) {
        try {
            String uploadFilesUrl = baseS3Url + "/upload";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultipartFile> requestEntity = new HttpEntity<>(file, headers);

            var filesResponse = restTemplate.postForEntity(uploadFilesUrl, requestEntity, List.class);

            if (filesResponse.getStatusCode().is2xxSuccessful() && filesResponse.getBody() != null) {
                uploadedFileUrl = filesResponse.getBody().toString();
                System.out.println("File uploaded successfully: " + uploadedFileUrl);
            } else {
                throw new RuntimeException("Failed to upload file.");
            }
        } catch (Exception e) {
            System.err.println("Error during saga execution: " + e.getMessage());

            if (!uploadedFileUrl.isEmpty()) {
                String deleteFilesUrl = baseS3Url + "/delete";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> deleteRequest = new HttpEntity<>(uploadedFileUrl, headers);

                try {
                    restTemplate.postForEntity(deleteFilesUrl, deleteRequest, String.class);
                    System.out.println("Compensating action: Uploaded file to S3 deleted successfully.");
                } catch (Exception deleteException) {
                    System.err.println("Failed to delete uploaded file during compensation: " + deleteException.getMessage());
                }
            }

            throw new RuntimeException("Could not upload file to S3");
        }
    }

    public void tryUploadToDB(String baseAttractionUrl, String baseS3Url) {
        try {
            String uploadQueryUrl = baseAttractionUrl + "/upload";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<String> requestEntity = new HttpEntity<>(uploadedFileUrl, headers);

            var queryResponse = restTemplate.postForEntity(uploadQueryUrl, requestEntity, List.class);

            if (queryResponse.getStatusCode().is2xxSuccessful() && queryResponse.getBody() != null) {
                System.out.println("File uploaded successfully: " + uploadedFileUrl);
            } else {
                throw new RuntimeException("Failed to upload file to db.");
            }

        } catch (Exception e) {
            System.err.println("Error during saga execution: " + e.getMessage());

            // if not so try to delete leftover from S3
            if (!uploadedFileUrl.isEmpty()) {
                String deleteFileUrl = baseS3Url + "/delete";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> deleteRequest = new HttpEntity<>(uploadedFileUrl, headers);

                try {
                    restTemplate.postForEntity(deleteFileUrl, deleteRequest, String.class);
                    System.out.println("Compensating action: Uploaded file to db deleted successfully.");
                } catch (Exception deleteException) {
                    System.err.println("Failed to delete uploaded file from db during compensation: " + deleteException.getMessage());
                }
            }

            throw new RuntimeException("Saga failed and compensating actions were executed.");
        }
    }
}