package org.example.saga.Saga.application.responsibilities;

import org.apache.commons.io.IOUtils;
import org.example.saga.Saga.dto.Attraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class Delete {
    private final RestTemplate restTemplate;
    private final Upload uploadOperation;

    private List<String> linksPreview = new ArrayList<>();
    private List<String> linksBefore = new ArrayList<>();
    private List<String> linksIn = new ArrayList<>();
    private List<String> linksAfter = new ArrayList<>();

    @Value("${base.attraction.url}")
    private String baseAttractionUrl;

    @Value("${base.s3.url}")
    private String baseS3Url;

    @Autowired
    public Delete(RestTemplate restTemplate, Upload uploadOperation) {
        this.restTemplate = restTemplate;
        this.uploadOperation = uploadOperation;
    }

    public void tryDeleteFromS3_batchFiles(List<String> fileUrls) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(baseS3Url + "/batch-delete")
                    .queryParam("urls", fileUrls.toArray())
                    .build()
                    .toUri();

            restTemplate.exchange(uri, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

            System.out.println("Files deleted successfully from S3.");
        } catch (Exception e) {
            System.err.println("Error while deleting from S3: " + e.getMessage());
        }
    }

    public void tryDeleteFromDB(Long id) {
        try {
            restTemplate.delete(baseAttractionUrl + "/" + id);
        } catch (Exception e) {
            System.err.println("Error while deleting from DB: " + e.getMessage());

            List<MultipartFile> links_preview = new ArrayList<>();
            List<MultipartFile> links_before = new ArrayList<>();
            List<MultipartFile> links_in = new ArrayList<>();
            List<MultipartFile> links_after = new ArrayList<>();

            for(String preview : linksPreview) {
                links_preview.add(convertUrlToMultipartFile(preview));
            }
            for(String before : linksBefore) {
                links_before.add(convertUrlToMultipartFile(before));
            }
            for(String in : linksIn) {
                links_in.add(convertUrlToMultipartFile(in));
            }
            for(String after : linksAfter) {
                links_after.add(convertUrlToMultipartFile(after));
            }

            uploadOperation.tryUploadToS3_batchFiles(links_preview, "preview");
            uploadOperation.tryUploadToS3_batchFiles(links_before, "before");
            uploadOperation.tryUploadToS3_batchFiles(links_in, "in");
            uploadOperation.tryUploadToS3_batchFiles(links_after, "after");
        }
    }

    public void tryFindByById(Long id) {
        try {
            var response = restTemplate.getForEntity(baseAttractionUrl + "/" + id, Attraction.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                var attraction = response.getBody();

                tryDeleteFromS3_batchFiles(attraction.getLinksPreview());
                tryDeleteFromS3_batchFiles(attraction.getLinksIn());
                tryDeleteFromS3_batchFiles(attraction.getLinksBefore());
                tryDeleteFromS3_batchFiles(attraction.getLinksAfter());

                linksPreview = attraction.getLinksPreview();
                linksIn = attraction.getLinksIn();
                linksBefore = attraction.getLinksBefore();
                linksAfter = attraction.getLinksAfter();
            } else {
                System.out.println("Error while getting the entity: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Could not get the entity from db: " + e);
        }
    }

    private MultipartFile convertUrlToMultipartFile(String fileUrl) {
        try {
            // Открываем соединение
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Ошибка загрузки файла с URL: " + fileUrl);
            }

            // Читаем данные в массив байтов
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] fileBytes = IOUtils.toByteArray(inputStream);
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

                return new MockMultipartFile(
                        "file",
                        fileName,
                        MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        fileBytes
                );
            }
        } catch (IOException e) {
            System.err.println("Ошибка при конвертации URL в MultipartFile: " + e.getMessage());
            return null;
        }
    }
}
