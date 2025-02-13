package org.example.saga.Saga.application.services;

import org.apache.commons.io.IOUtils;
import org.example.saga.Saga.application.responsibilities.Delete;
import org.example.saga.Saga.application.responsibilities.Upload;
import org.example.saga.Saga.dto.Attraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
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

@Service
public class SagaService {
    private final Delete deleteOperation;
    private final Upload uploadOperation;

    @Autowired
    public SagaService(Delete deleteOperation, Upload uploadOperation) {
        this.deleteOperation = deleteOperation;
        this.uploadOperation = uploadOperation;
    }

    public void tryDelete_batchFiles(List<Long> ids) {
        for(Long id : ids) {
            deleteOperation.tryFindByById(id);
        }

        for(Long id : ids) {
            deleteOperation.tryDeleteFromDB(id);
        }
    }

    public void tryDelete_file(Long id) {
        deleteOperation.tryFindByById(id);
        deleteOperation.tryDeleteFromDB(id);
    }

    public void tryUpload_file(MultipartFile file, Attraction attraction) {
        uploadOperation.tryUploadToS3_file(file);
        uploadOperation.tryUploadToDB_file(attraction);
    }

    public void tryUpload_batchFiles(List<MultipartFile> files, List<Attraction> attractions) {
        uploadOperation.tryUploadToS3_batchFiles(files);
        for (Attraction attraction : attractions) {
            uploadOperation.tryUploadToDB_file(attraction);
        }
    }
}
