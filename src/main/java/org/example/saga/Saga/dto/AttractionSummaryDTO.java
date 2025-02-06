package org.example.saga.Saga.dto;

public class AttractionSummaryDTO {
    private Long id;

    public AttractionSummaryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}