package org.example.saga.Saga.application.services;

import org.example.saga.Saga.application.responsibilities.Delete;
import org.example.saga.Saga.application.responsibilities.Upload;
import org.example.saga.Saga.dto.Attraction;
import org.example.saga.Saga.dto.ReceivedAttraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Сервисный класс SagaService, который отвечает за обработку операций удаления и загрузки файлов.
 * Взаимодействует с компонентами Delete и Upload для выполнения соответствующих операций.
 */
@Service
public class SagaService {
    private final Delete deleteOperation; // Компонент для выполнения операций удаления
    private final Upload uploadOperation; // Компонент для выполнения операций загрузки

    /**
     * Конструктор для внедрения зависимостей Delete и Upload.
     *
     * @param deleteOperation Компонент для операций удаления
     * @param uploadOperation Компонент для операций загрузки
     */
    @Autowired
    public SagaService(Delete deleteOperation, Upload uploadOperation) {
        this.deleteOperation = deleteOperation;
        this.uploadOperation = uploadOperation;
    }

    /**
     * Метод для удаления нескольких файлов по их идентификаторам.
     * Сначала проверяет наличие файлов в базе данных, затем удаляет их.
     *
     * @param ids Список идентификаторов файлов для удаления
     */
    public void tryDelete_batchFiles(List<Long> ids) {
        for (Long id : ids) {
            deleteOperation.tryFindByById(id); // Проверка наличия файла в базе данных
        }

        for (Long id : ids) {
            deleteOperation.tryDeleteFromDB(id); // Удаление файла из базы данных
        }
    }

    /**
     * Метод для удаления одного файла по его идентификатору.
     * Сначала проверяет наличие файла в базе данных, затем удаляет его.
     *
     * @param id Идентификатор файла для удаления
     */
    public void tryDelete_file(Long id) {
        deleteOperation.tryFindByById(id); // Проверка наличия файла в базе данных
        deleteOperation.tryDeleteFromDB(id); // Удаление файла из базы данных
    }

    /**
     * Метод для загрузки файлов, связанных с достопримечательностью, и сохранения информации о ней.
     * Загружает файлы в S3 и сохраняет информацию о достопримечательности в базе данных.
     *
     * @param receivedAttraction Объект, содержащий информацию о достопримечательности и ссылки на файлы
     */
    public void tryUpload_batchFiles(ReceivedAttraction receivedAttraction) {
        // Загрузка файлов в S3 для каждого типа (preview, before, in, after)
        uploadOperation.tryUploadToS3_batchFiles(receivedAttraction.getLinksPreview(), "preview");
        uploadOperation.tryUploadToS3_batchFiles(receivedAttraction.getLinksBefore(), "before");
        uploadOperation.tryUploadToS3_batchFiles(receivedAttraction.getLinksIn(), "in");
        uploadOperation.tryUploadToS3_batchFiles(receivedAttraction.getLinksAfter(), "after");

        // Создание объекта Attraction на основе полученных данных
        Attraction attraction = new Attraction(
                receivedAttraction.getId(),
                receivedAttraction.getName(),
                receivedAttraction.getDescriptionBefore(),
                receivedAttraction.getDescriptionIn(),
                receivedAttraction.getDescriptionAfter(),
                receivedAttraction.getInterestingFacts(),
                receivedAttraction.getYearOfCreation(),
                receivedAttraction.getLocation()
        );

        // Сохранение информации о достопримечательности в базе данных
        uploadOperation.tryUploadToDB_file(attraction);
    }
}