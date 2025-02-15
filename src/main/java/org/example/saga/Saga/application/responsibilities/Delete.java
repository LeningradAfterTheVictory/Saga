package org.example.saga.Saga.application.responsibilities;

import org.apache.commons.io.IOUtils;
import org.example.saga.Saga.dto.Attraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Класс Delete отвечает за удаление файлов из S3 и данных о достопримечательностях из базы данных.
 * Также реализует механизм компенсации (восстановление удаленных файлов) в случае ошибок.
 */
@Component
public class Delete {
    private static final Logger logger = LoggerFactory.getLogger(Delete.class);

    private final RestTemplate restTemplate; // Клиент для выполнения HTTP-запросов
    private final Upload uploadOperation; // Компонент для загрузки файлов (используется для компенсации)

    // Списки для хранения ссылок на файлы, которые были удалены (для компенсации)
    private List<String> linksPreview = new ArrayList<>();
    private List<String> linksBefore = new ArrayList<>();
    private List<String> linksIn = new ArrayList<>();
    private List<String> linksAfter = new ArrayList<>();

    @Value("${base.attraction.url}") // URL для работы с данными о достопримечательностях
    private String baseAttractionUrl;

    @Value("${base.s3.url}") // URL для работы с файлами в S3
    private String baseS3Url;

    /**
     * Конструктор для внедрения зависимостей RestTemplate и Upload.
     *
     * @param restTemplate   Клиент для выполнения HTTP-запросов
     * @param uploadOperation Компонент для загрузки файлов (используется для компенсации)
     */
    @Autowired
    public Delete(RestTemplate restTemplate, Upload uploadOperation) {
        this.restTemplate = restTemplate;
        this.uploadOperation = uploadOperation;
    }

    /**
     * Метод для удаления нескольких файлов из S3 по их URL.
     *
     * @param fileUrls Список URL файлов для удаления
     */
    public void tryDeleteFromS3_batchFiles(List<String> fileUrls) {
        try {
            // Формируем URI для запроса на удаление файлов
            URI uri = UriComponentsBuilder.fromUriString(baseS3Url + "/batch-delete")
                    .queryParam("urls", fileUrls.toArray())
                    .build()
                    .toUri();

            // Выполняем DELETE-запрос для удаления файлов
            restTemplate.exchange(uri, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

            logger.info("Files deleted successfully from S3.");
        } catch (Exception e) {
            logger.error("Error while deleting from S3: " + e.getMessage());
        }
    }

    /**
     * Метод для удаления данных о достопримечательности из базы данных по её идентификатору.
     * В случае ошибки выполняется компенсация (восстановление удаленных файлов в S3).
     *
     * @param id Идентификатор достопримечательности
     */
    public void tryDeleteFromDB(Long id) {
        try {
            // Выполняем DELETE-запрос для удаления данных из базы данных
            restTemplate.delete(baseAttractionUrl + "/" + id);
        } catch (Exception e) {
            logger.error("Error while deleting from DB: " + e.getMessage());

            // Если произошла ошибка, восстанавливаем удаленные файлы в S3 (компенсация)
            List<MultipartFile> links_preview = new ArrayList<>();
            List<MultipartFile> links_before = new ArrayList<>();
            List<MultipartFile> links_in = new ArrayList<>();
            List<MultipartFile> links_after = new ArrayList<>();

            // Конвертируем URL в MultipartFile для загрузки
            for (String preview : linksPreview) {
                links_preview.add(convertUrlToMultipartFile(preview));
            }
            for (String before : linksBefore) {
                links_before.add(convertUrlToMultipartFile(before));
            }
            for (String in : linksIn) {
                links_in.add(convertUrlToMultipartFile(in));
            }
            for (String after : linksAfter) {
                links_after.add(convertUrlToMultipartFile(after));
            }

            // Загружаем файлы обратно в S3
            uploadOperation.tryUploadToS3_batchFiles(links_preview, "preview");
            uploadOperation.tryUploadToS3_batchFiles(links_before, "before");
            uploadOperation.tryUploadToS3_batchFiles(links_in, "in");
            uploadOperation.tryUploadToS3_batchFiles(links_after, "after");
        }
    }

    /**
     * Метод для поиска достопримечательности по её идентификатору.
     * Если достопримечательность найдена, удаляются связанные с ней файлы из S3.
     *
     * @param id Идентификатор достопримечательности
     */
    public void tryFindByById(Long id) {
        try {
            // Выполняем GET-запрос для получения данных о достопримечательности
            var response = restTemplate.getForEntity(baseAttractionUrl + "/" + id, Attraction.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                var attraction = response.getBody();

                // Удаляем файлы из S3
                tryDeleteFromS3_batchFiles(attraction.getLinksPreview());
                tryDeleteFromS3_batchFiles(attraction.getLinksIn());
                tryDeleteFromS3_batchFiles(attraction.getLinksBefore());
                tryDeleteFromS3_batchFiles(attraction.getLinksAfter());

                // Сохраняем ссылки на файлы для возможной компенсации
                linksPreview = attraction.getLinksPreview();
                linksIn = attraction.getLinksIn();
                linksBefore = attraction.getLinksBefore();
                linksAfter = attraction.getLinksAfter();
            } else {
                logger.error("Error while getting the entity: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Could not get the entity from db: " + e);
        }
    }

    /**
     * Метод для конвертации URL файла в объект MultipartFile.
     * Используется для восстановления файлов в S3 в случае компенсации.
     *
     * @param fileUrl URL файла
     * @return MultipartFile, представляющий файл
     */
    private MultipartFile convertUrlToMultipartFile(String fileUrl) {
        try {
            // Открываем соединение с URL
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Проверяем успешность соединения
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Ошибка загрузки файла с URL: " + fileUrl);
            }

            // Читаем данные файла в массив байтов
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] fileBytes = IOUtils.toByteArray(inputStream);
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

                // Создаем объект MultipartFile
                return new MockMultipartFile(
                        "file",
                        fileName,
                        MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        fileBytes
                );
            }
        } catch (IOException e) {
            logger.error("Ошибка при конвертации URL в MultipartFile: " + e.getMessage());
            return null;
        }
    }
}