package org.example.saga.Saga.dto;

public class DeleteSingleIdentifierDTO {
    private Long id;

    public DeleteSingleIdentifierDTO(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}