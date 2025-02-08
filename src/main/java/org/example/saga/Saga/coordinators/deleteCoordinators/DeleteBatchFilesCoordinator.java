package org.example.saga.Saga.coordinators.deleteCoordinators;

import org.apache.commons.io.IOUtils;
import org.example.saga.Saga.coordinators.uploadCoordinators.UploadBatchFilesCoordinator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeleteBatchFilesCoordinator {
    private final RestTemplate restTemplate;
    private final UploadBatchFilesCoordinator filesCoordinator;

    @Value("${base.attraction.url}")
    private String baseQueryUrl;

    @Value("${base.s3.url}")
    private String baseS3Url;

    public DeleteBatchFilesCoordinator() {
        this.restTemplate = new RestTemplate();
        this.filesCoordinator = new UploadBatchFilesCoordinator();
    }

    public void tryDelete(List<String> fileUrls, Long id) {
        tryDeleteFromS3(fileUrls);
        tryDeleteFromDB(fileUrls, id);
    }

    public void tryDeleteFromS3(List<String> fileUrls) {
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

    public void tryDeleteFromDB(List<String> fileUrls, Long id) {
        try {
            for (String fileUrl : fileUrls) {
                restTemplate.delete(baseQueryUrl + "/" + id);
            }
        } catch (Exception e) {
            System.err.println("Error while deleting from DB: " + e.getMessage());

            List<MultipartFile> files = new ArrayList<>();
            for (String fileUrl : fileUrls) {
                MultipartFile file = convertUrlToMultipartFile(fileUrl);
                files.add(file);
            }

            filesCoordinator.tryExtractingViaS3(baseS3Url, files);
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