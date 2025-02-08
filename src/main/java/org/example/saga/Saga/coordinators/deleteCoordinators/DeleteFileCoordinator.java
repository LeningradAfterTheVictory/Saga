package org.example.saga.Saga.coordinators.deleteCoordinators;

import org.apache.commons.io.IOUtils;
import org.example.saga.Saga.coordinators.uploadCoordinators.UploadFileCoordinator;
import org.example.saga.Saga.dto.Attraction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
public class DeleteFileCoordinator {
    private final RestTemplate restTemplate;
    private final UploadFileCoordinator fileCoordinator;
    private final DeleteBatchFilesCoordinator deleteFilesCoordinator;

    private List<String> linksPreview;
    private List<String> linksBefore;
    private List<String> linksIn;
    private List<String> linksAfter;

    @Value("${base.attraction.url}")
    private String baseAttractionUrl;

    @Value("${base.s3.url}")
    private String baseS3Url;

    public DeleteFileCoordinator() {
        this.restTemplate = new RestTemplate();
        this.fileCoordinator = new UploadFileCoordinator();
        this.deleteFilesCoordinator = new DeleteBatchFilesCoordinator();
    }

    public void tryDelete(String fileUrl, Long id) {
        tryFindByById(id);
        tryDeleteFromDB(baseS3Url, baseAttractionUrl, fileUrl, id);
    }

    private void tryFindByById(Long id) {
        try {
            var response = restTemplate.getForEntity(baseAttractionUrl + "/" + id, Attraction.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                var attraction = response.getBody();

                deleteFilesCoordinator.tryDeleteFromS3(attraction.getLinksPreview());
                deleteFilesCoordinator.tryDeleteFromS3(attraction.getLinksIn());
                deleteFilesCoordinator.tryDeleteFromS3(attraction.getLinksBefore());
                deleteFilesCoordinator.tryDeleteFromS3(attraction.getLinksAfter());

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
