package org.example.saga.Saga.coordinators.deleteCoordinators;

import org.apache.commons.io.IOUtils;
import org.example.saga.Saga.coordinators.uploadCoordinators.UploadFileCoordinator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeleteFileCoordinator {
    private final RestTemplate restTemplate;
    private final UploadFileCoordinator fileCoordinator;

    @Value("${base.attraction.url}")
    private String baseAttractionUrl;

    @Value("${base.s3.url}")
    private String baseS3Url;

    public DeleteFileCoordinator() {
        this.restTemplate = new RestTemplate();
        this.fileCoordinator = new UploadFileCoordinator();
    }

    public void tryDelete(String fileUrl, Long id) {
        tryDeleteFromS3(baseS3Url, fileUrl);
        tryDeleteFromDB(baseS3Url, baseAttractionUrl, fileUrl, id);
    }

    private void tryDeleteFromS3(String baseS3Url, String fileUrl) {
        try {
            restTemplate.delete(baseS3Url + fileUrl);
        } catch (Exception e) {
            System.err.println("Error while deleting from S3: " + e.getMessage());
        }
    }

    private void tryDeleteFromDB(String baseS3Url, String baseAttractionUrl, String fileUrl, Long id) {
        try {
            restTemplate.delete(baseAttractionUrl + "/" + id);
        } catch (Exception e) {
            System.err.println("Error while deleting from DB: " + e.getMessage());

            MultipartFile file = convertUrlToMultipartFile(fileUrl);
            if (file != null) {
                fileCoordinator.tryExtractingViaS3(baseS3Url, file);
            }
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
