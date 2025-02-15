package org.example.saga.Saga.application.responsibilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.saga.Saga.dto.Attraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Repository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс Upload отвечает за загрузку файлов в S3 и сохранение информации о достопримечательностях в базе данных.
 * Также реализует механизм компенсации (удаление загруженных файлов) в случае ошибок.
 */
@Repository
public class Upload {
    private static final Logger logger = LoggerFactory.getLogger(Upload.class);
    private final RestTemplate restTemplate; // Клиент для выполнения HTTP-запросов

    @Value("${base.attraction.url}") // URL для сохранения данных о достопримечательности
    private String baseAttractionUrl;

    @Value("${base.s3.url}") // URL для загрузки файлов в S3
    private String baseS3Url;

    // Списки для хранения ссылок на загруженные файлы по типам
    private final List<String> uploadedLinksPreview = new ArrayList<>();
    private final List<String> uploadedLinksBefore = new ArrayList<>();
    private final List<String> uploadedLinksIn = new ArrayList<>();
    private final List<String> uploadedLinksAfter = new ArrayList<>();

    /**
     * Конструктор для внедрения зависимости RestTemplate.
     *
     * @param restTemplate Клиент для выполнения HTTP-запросов
     */
    @Autowired
    public Upload(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Метод для загрузки пакета файлов в S3.
     * После успешной загрузки ссылки на файлы сохраняются в соответствующий список.
     *
     * @param files Список файлов для загрузки
     * @param type  Тип файлов (preview, before, in, after)
     */
    public void tryUploadToS3_batchFiles(List<MultipartFile> files, String type) {
        String uploadFilesUrl = baseS3Url + "/batch-upload"; // URL для загрузки файлов в S3

        // Создаем MultiValueMap для передачи файлов
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            body.add("photos", new FileSystemResource(convertMultipartFileToFile(file)));
        }

        // Устанавливаем заголовки
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Формируем запрос
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Выполняем запрос на загрузку файлов
        var filesResponse = restTemplate.postForEntity(uploadFilesUrl, requestEntity, List.class);

        if (filesResponse.getStatusCode().is2xxSuccessful() && filesResponse.getBody() != null) {
            // В зависимости от типа файлов сохраняем ссылки в соответствующий список
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
            logger.info("Files uploaded successfully: " + filesResponse.getBody().toString());
        } else {
            throw new RuntimeException("Failed to upload files."); // В случае ошибки выбрасываем исключение
        }
    }

    /**
     * Метод для сохранения информации о достопримечательности в базе данных.
     * Если происходит ошибка, выполняется компенсация (удаление загруженных файлов из S3).
     *
     * @param attraction Объект с информацией о достопримечательности
     */
    public void tryUploadToDB_file(Attraction attraction) {
        // Устанавливаем ссылки на загруженные файлы в объект Attraction
        attraction.setLinksPreview(uploadedLinksPreview);
        attraction.setLinksBefore(uploadedLinksBefore);
        attraction.setLinksIn(uploadedLinksIn);
        attraction.setLinksAfter(uploadedLinksAfter);

        try {
            String uploadQueryUrl = baseAttractionUrl; // URL для сохранения данных о достопримечательности
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // Устанавливаем тип содержимого запроса

            HttpEntity<Attraction> requestEntity = new HttpEntity<>(attraction, headers); // Формируем запрос

            // Выполняем запрос на сохранение данных о достопримечательности
            var queryResponse = restTemplate.postForEntity(uploadQueryUrl, requestEntity, Void.class);

            if (queryResponse.getStatusCode().is2xxSuccessful()) {
                logger.info("Attraction saved successfully");
            } else {
                throw new RuntimeException("Failed to upload file to db."); // В случае ошибки выбрасываем исключение
            }

        } catch (Exception e) {
            logger.error("Error during saga execution: " + e.getMessage());

            // Если произошла ошибка, удаляем все загруженные файлы из S3 (компенсация)
            deleteFilesFromS3(attraction.getLinksPreview());
            deleteFilesFromS3(attraction.getLinksBefore());
            deleteFilesFromS3(attraction.getLinksIn());
            deleteFilesFromS3(attraction.getLinksAfter());
        }
    }

    /**
     * Метод для удаления файлов из S3 в случае ошибки (компенсация).
     *
     * @param fileUrls Список ссылок на файлы для удаления
     */
    private void deleteFilesFromS3(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return; // Если список пуст, ничего не делаем
        }

        String deleteFileUrl = baseS3Url + "/delete"; // URL для удаления файлов из S3
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Устанавливаем тип содержимого запроса

        // Удаляем каждый файл по его ссылке
        for (String fileUrl : fileUrls) {
            try {
                HttpEntity<String> deleteRequest = new HttpEntity<>(fileUrl, headers); // Формируем запрос
                restTemplate.postForEntity(deleteFileUrl, deleteRequest, String.class); // Выполняем запрос
                logger.info("Compensating action: Uploaded file " + fileUrl + " deleted successfully.");
            } catch (Exception deleteException) {
                logger.error("Failed to delete uploaded file " + fileUrl + " from db during compensation: " + deleteException.getMessage());
            }
        }
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) {
        try {
            File file = File.createTempFile("temp", null);
            multipartFile.transferTo(file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert MultipartFile to File", e);
        }
    }
}