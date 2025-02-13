package org.example.saga.Saga.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.saga.Saga.application.services.SagaService;
import org.example.saga.Saga.dto.Attraction;
import org.springframework.beans.factory.annotation.Autowired;
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
            @RequestBody List<Long> ids) {
        _sagaService.tryDelete_batchFiles(ids);
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
            @RequestBody Long id) {
        _sagaService.tryDelete_file(id);
    }

    @PostMapping("/upload")
    @Operation(summary = "Загрузить файл", description = "Загружает файл и связывает его с достопримечательностью")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл успешно загружен"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public void tryUpload_file(
            @Parameter(description = "Файл для загрузки", required = true)
            @RequestBody MultipartFile file,
            @Parameter(description = "Достопримечательность, связанная с файлом", required = true)
            @RequestBody Attraction attraction) {
        _sagaService.tryUpload_file(file, attraction);
    }

    @PostMapping("/batch-upload")
    @Operation(summary = "Загрузить несколько файлов", description = "Загружает несколько файлов и связывает их с достопримечательностями")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файлы успешно загружены"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public void tryUpload_batchFiles(
            @Parameter(description = "Список файлов для загрузки", required = true)
            @RequestBody List<MultipartFile> files,
            @Parameter(description = "Список достопримечательностей, связанных с файлами", required = true)
            @RequestBody List<Attraction> attractions) {
        _sagaService.tryUpload_batchFiles(files, attractions);
    }
}