package org.example.saga.Saga.dto;

import org.locationtech.jts.geom.Point;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ReceivedAttraction {
    private Long id;
    private String name;
    private String descriptionBefore;
    private String descriptionIn;
    private String descriptionAfter;
    private String interestingFacts;
    private Integer yearOfCreation;
    private Point location;
    private List<MultipartFile> linksPreview;
    private List<MultipartFile> linksBefore;
    private List<MultipartFile> linksIn;
    private List<MultipartFile> linksAfter;

    public ReceivedAttraction(Long id, String name,
                      String descriptionBefore,
                      String descriptionIn, String descriptionAfter,
                      String interestingFacts, Integer yearOfCreation,
                      Point location, List<MultipartFile> linksPreview,
                      List<MultipartFile> linksBefore,
                      List<MultipartFile> linksIn, List<MultipartFile> linksAfter) {
        this.id = id;
        this.name = name;
        this.descriptionBefore = descriptionBefore;
        this.descriptionIn = descriptionIn;
        this.descriptionAfter = descriptionAfter;
        this.interestingFacts = interestingFacts;
        this.yearOfCreation = yearOfCreation;
        this.location = location;
        this.linksPreview = linksPreview;
        this.linksBefore = linksBefore;
        this.linksIn = linksIn;
        this.linksAfter = linksAfter;
    }


    public List<MultipartFile> getLinksPreview() {
        return linksPreview;
    }

    public void setLinksPreview(List<MultipartFile> linksPreview) {
        this.linksPreview = linksPreview;
    }

    public void addToLinksPreview(MultipartFile preview) {
        this.linksPreview.add(preview);
    }

    public void addToLinksBefore(MultipartFile before) {
        this.linksBefore.add(before);
    }

    public void addToLinksIn(MultipartFile in) {
        this.linksIn.add(in);
    }

    public void addToLinksAfter(MultipartFile after) {
        this.linksAfter.add(after);
    }


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

    public String getDescriptionBefore() {
        return descriptionBefore;
    }

    public void setDescriptionBefore(String descriptionBefore) {
        this.descriptionBefore = descriptionBefore;
    }

    public String getDescriptionIn() {
        return descriptionIn;
    }

    public void setDescriptionIn(String descriptionIn) {
        this.descriptionIn = descriptionIn;
    }

    public String getDescriptionAfter() {
        return descriptionAfter;
    }

    public void setDescriptionAfter(String descriptionAfter) {
        this.descriptionAfter = descriptionAfter;
    }

    public String getInterestingFacts() {
        return interestingFacts;
    }

    public void setInterestingFacts(String interestingFacts) {
        this.interestingFacts = interestingFacts;
    }

    public Integer getYearOfCreation() {
        return yearOfCreation;
    }

    public void setYearOfCreation(Integer yearOfCreation) {
        this.yearOfCreation = yearOfCreation;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public List<MultipartFile> getLinksBefore() {
        return linksBefore;
    }

    public void setLinksBefore(List<MultipartFile> linksBefore) {
        this.linksBefore = linksBefore;
    }

    public List<MultipartFile> getLinksIn() {
        return linksIn;
    }

    public void setLinksIn(List<MultipartFile> linksIn) {
        this.linksIn = linksIn;
    }

    public List<MultipartFile> getLinksAfter() {
        return linksAfter;
    }

    public void setLinksAfter(List<MultipartFile> linksAfter) {
        this.linksAfter = linksAfter;
    }
}
