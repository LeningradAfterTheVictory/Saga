package org.example.saga.Saga.application.responsibilities;

import org.example.saga.Saga.dto.Attraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Repository
public class Upload {
    private final RestTemplate restTemplate;

    @Value("${base.attraction.url}")
    private String baseAttractionUrl;

    @Value("${base.s3.url}")
    private String baseS3Url;


    private final List<String> uploadedSingleFileUrl = new ArrayList<>();

    private final List<String> uploadedLinksPreview = new ArrayList<>();
    private final List<String> uploadedLinksBefore = new ArrayList<>();
    private final List<String> uploadedLinksIn = new ArrayList<>();
    private final List<String> uploadedLinksAfter = new ArrayList<>();

    @Autowired
    public Upload(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void tryUploadToS3_file(MultipartFile file, String type) {
        String uploadFilesUrl = baseS3Url + "/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultipartFile> requestEntity = new HttpEntity<>(file, headers);

        var filesResponse = restTemplate.postForEntity(uploadFilesUrl, requestEntity, List.class);

        if (filesResponse.getStatusCode().is2xxSuccessful() && filesResponse.getBody() != null) {
            uploadedSingleFileUrl.add(filesResponse.getBody().toString());
            System.out.println("File uploaded successfully: " + filesResponse.getBody().toString());
        } else {
            throw new RuntimeException("Failed to upload file.");
        }
    }

    public void tryUploadToS3_batchFiles(List<MultipartFile> files, String type) {
        String uploadFilesUrl = baseS3Url + "/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<List<MultipartFile>> requestEntity = new HttpEntity<>(files, headers);

        var filesResponse = restTemplate.postForEntity(uploadFilesUrl, requestEntity, List.class);

        if (filesResponse.getStatusCode().is2xxSuccessful() && filesResponse.getBody() != null) {
            switch (type) {
                case "preview":
                    uploadedLinksPreview.addAll(filesResponse.getBody().stream().toList());
                    break;
                case "before":
                    uploadedLinksBefore.addAll(filesResponse.getBody().stream().toList());
                    break;
                case "in":
                    uploadedLinksIn.addAll(filesResponse.getBody().stream().toList());
                    break;
                case "after":
                    uploadedLinksAfter.addAll(filesResponse.getBody().stream().toList());
                    break;
            }
            System.out.println("File uploaded successfully: " + filesResponse.getBody().toString());
        } else {
            throw new RuntimeException("Failed to upload file.");
        }
    }

    public void tryUploadToDB_file(Attraction attraction) {
        if (uploadedSingleFileUrl.isEmpty()) {
            attraction.setLinksPreview(uploadedLinksPreview);
            attraction.setLinksBefore(uploadedLinksBefore);
            attraction.setLinksIn(uploadedLinksIn);
            attraction.setLinksAfter(uploadedLinksAfter);
        } else {
            attraction.addToLinksPreview(uploadedSingleFileUrl.get(0));
            attraction.addToLinksBefore(uploadedLinksBefore.get(1));
            attraction.addToLinksIn(uploadedLinksBefore.get(2));
            attraction.addToLinksAfter(uploadedLinksBefore.get(3));
        }
        try {
            String uploadQueryUrl = baseAttractionUrl;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Attraction> requestEntity = new HttpEntity<>(attraction, headers);

            var queryResponse = restTemplate.postForEntity(uploadQueryUrl, requestEntity, List.class);

            if (queryResponse.getStatusCode().is2xxSuccessful() && queryResponse.getBody() != null) {
                System.out.println("Attraction saved successfully");
            } else {
                throw new RuntimeException("Failed to upload file to db.");
            }

        } catch (Exception e) {
            System.err.println("Error during saga execution: " + e.getMessage());

            // Если произошла ошибка, удаляем все загруженные файлы из S3
            deleteFilesFromS3(attraction.getLinksPreview());
            deleteFilesFromS3(attraction.getLinksBefore());
            deleteFilesFromS3(attraction.getLinksIn());
            deleteFilesFromS3(attraction.getLinksAfter());
        }
    }

    private void deleteFilesFromS3(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        String deleteFileUrl = baseS3Url + "/delete";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (String fileUrl : fileUrls) {
            try {
                HttpEntity<String> deleteRequest = new HttpEntity<>(fileUrl, headers);
                restTemplate.postForEntity(deleteFileUrl, deleteRequest, String.class);
                System.out.println("Compensating action: Uploaded file " + fileUrl + " deleted successfully.");
            } catch (Exception deleteException) {
                System.err.println("Failed to delete uploaded file " + fileUrl + " from db during compensation: " + deleteException.getMessage());
            }
        }
    }
}
