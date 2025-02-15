package org.example.saga.Saga.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.saga.Saga.application.services.SagaService;
import org.example.saga.Saga.dto.Attraction;
import org.example.saga.Saga.dto.DeleteBatchFilesDTO;
import org.example.saga.Saga.dto.DeleteSingleIdentifierDTO;
import org.example.saga.Saga.dto.ReceivedAttraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/request")
@Tag(name = "Saga Controller", description = "Контроллер для управления файлами и достопримечательностями")
public class SagaController {
    private final SagaService _sagaService;

    @Autowired
    public SagaController(SagaService sagaService) {
        _sagaService = sagaService;
    }

    @DeleteMapping("/batch-delete")
    @Operation(summary = "Удалить несколько файлов", description = "Удаляет несколько файлов по их идентификаторам")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файлы успешно удалены"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public void tryDelete_batchFiles(
            @Parameter(description = "Список идентификаторов файлов для удаления", required = true)
            @RequestBody DeleteBatchFilesDTO deleteBatchFilesDTO) {
        _sagaService.tryDelete_batchFiles(deleteBatchFilesDTO.getIds());
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удалить файл", description = "Удаляет файл по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл успешно удален"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public void tryDelete_file(
            @Parameter(description = "Идентификатор файла для удаления", required = true)
            @RequestBody DeleteSingleIdentifierDTO deleteSingleIdentifierDTO) {
        _sagaService.tryDelete_file(deleteSingleIdentifierDTO.getId());
    }

    @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить достопремичательность", description = "Загружает файлы и сохраняет в достопремичательность")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файлы успешно загружены"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public void tryUpload_batchFiles(
            @Parameter(description = "Отправленная достопремичательность на обработку", required = true)
            @RequestPart("attraction") ReceivedAttraction receivedAttraction,
            @RequestPart("linksPreview") List<MultipartFile> linksPreview,
            @RequestPart("linksBefore") List<MultipartFile> linksBefore,
            @RequestPart("linksIn") List<MultipartFile> linksIn,
            @RequestPart("linksAfter") List<MultipartFile> linksAfter) {
        receivedAttraction.setLinksPreview(linksPreview);
        receivedAttraction.setLinksBefore(linksBefore);
        receivedAttraction.setLinksIn(linksIn);
        receivedAttraction.setLinksAfter(linksAfter);

        _sagaService.tryUpload_batchFiles(receivedAttraction);
    }
}