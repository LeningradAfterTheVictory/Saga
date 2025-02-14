package org.example.saga.Saga.application.services;

import org.example.saga.Saga.application.responsibilities.Delete;
import org.example.saga.Saga.application.responsibilities.Upload;
import org.example.saga.Saga.dto.Attraction;
import org.example.saga.Saga.dto.ReceivedAttraction;
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

    public void tryUpload_batchFiles(ReceivedAttraction receivedAttraction) {
        uploadOperation.tryUploadToS3_batchFiles(receivedAttraction.getLinksPreview(), "preview");
        uploadOperation.tryUploadToS3_batchFiles(receivedAttraction.getLinksBefore(), "before");
        uploadOperation.tryUploadToS3_batchFiles(receivedAttraction.getLinksIn(), "in");
        uploadOperation.tryUploadToS3_batchFiles(receivedAttraction.getLinksAfter(), "after");

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

        uploadOperation.tryUploadToDB_file(attraction);
    }
}
