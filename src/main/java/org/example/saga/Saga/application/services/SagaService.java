package org.example.saga.Saga.application.services;

import org.example.saga.Saga.application.responsibilities.Delete;
import org.example.saga.Saga.application.responsibilities.Upload;
import org.example.saga.Saga.dto.Attraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;



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

    public void tryUpload_file(
            MultipartFile filePreview,
            MultipartFile fileBefore,
            MultipartFile fileIn,
            MultipartFile fileAfter,
            Attraction attraction) {
        uploadOperation.tryUploadToS3_file(filePreview, "preview");
        uploadOperation.tryUploadToS3_file(fileBefore, "before");
        uploadOperation.tryUploadToS3_file(fileIn, "in");
        uploadOperation.tryUploadToS3_file(fileAfter, "after");
        
        uploadOperation.tryUploadToDB_file(attraction);
    }

    public void tryUpload_batchFiles(
            List<MultipartFile> filesPreview,
            List<MultipartFile> filesBefore,
            List<MultipartFile> filesIn,
            List<MultipartFile> filesAfter,
            List<Attraction> attractions) {
        uploadOperation.tryUploadToS3_batchFiles(filesPreview, "preview");
        uploadOperation.tryUploadToS3_batchFiles(filesBefore, "before");
        uploadOperation.tryUploadToS3_batchFiles(filesIn, "in");
        uploadOperation.tryUploadToS3_batchFiles(filesAfter, "after");

        for (Attraction attraction : attractions) {
            uploadOperation.tryUploadToDB_file(attraction);
        }
    }
}
