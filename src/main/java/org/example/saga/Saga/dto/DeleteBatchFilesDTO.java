package org.example.saga.Saga.dto;

import java.util.List;

public class DeleteBatchFilesDTO {
    private List<Long> ids;

    public DeleteBatchFilesDTO(List<Long> ids) {
        this.ids = ids;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
