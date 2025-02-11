package org.example.saga.Saga.presentation.controllers;

import org.example.saga.Saga.application.services.SagaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/request")
public class SagaController {
    private final SagaService _sagaService;

    @Autowired
    public SagaController(SagaService sagaService) {
        _sagaService = sagaService;
    }

    @DeleteMapping("/batch-delete")
    public void tryDelete_batchFiles(
            @RequestParam("identifiers") List<Long> ids
    ) {
        _sagaService.tryDelete_batchFiles(ids);
    }

    @DeleteMapping("/delete")
    public void tryDelete_file(
            @RequestParam("identifier") Long id) {
        _sagaService.tryDelete_file(id);
    }

    @PostMapping("/upload")
    public void tryUpload_file(
            @RequestParam("file") MultipartFile file
    ) {
        _sagaService.tryUpload_file(file);
    }

    @PostMapping("/batch-upload")
    public void tryUpload_batchFiles(
            @RequestParam("files") List<MultipartFile> files) {
        _sagaService.tryUpload_batchFiles(files);
    }
}
