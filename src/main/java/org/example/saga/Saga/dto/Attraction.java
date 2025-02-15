package org.example.saga.Saga.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.example.saga.Saga.presentation.deserializers.PointDeserializer;
import org.example.saga.Saga.presentation.serializers.PointSerializer;
import org.locationtech.jts.geom.Point;

import java.util.List;

public class Attraction {
    private Long id;
    private String name;
    private String descriptionBefore;
    private String descriptionIn;
    private String descriptionAfter;
    private String interestingFacts;
    private Integer yearOfCreation;

    @JsonSerialize(using = PointSerializer.class)
    @JsonDeserialize(using = PointDeserializer.class)
    private Point location;

    private List<String> linksPreview;
    private List<String> linksBefore;
    private List<String> linksIn;
    private List<String> linksAfter;

    public Attraction(Long id, String name,
                      String descriptionBefore,
                      String descriptionIn, String descriptionAfter,
                      String interestingFacts, Integer yearOfCreation,
                      Point location) {
        this.id = id;
        this.name = name;
        this.descriptionBefore = descriptionBefore;
        this.descriptionIn = descriptionIn;
        this.descriptionAfter = descriptionAfter;
        this.interestingFacts = interestingFacts;
        this.yearOfCreation = yearOfCreation;
        this.location = location;
    }


    public List<String> getLinksPreview() {
        return linksPreview;
    }

    public void setLinksPreview(List<String> linksPreview) {
        this.linksPreview = linksPreview;
    }

    public void addToLinksPreview(String preview) {
        this.linksPreview.add(preview);
    }

    public void addToLinksBefore(String before) {
        this.linksBefore.add(before);
    }

    public void addToLinksIn(String in) {
        this.linksIn.add(in);
    }

    public void addToLinksAfter(String after) {
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

    public List<String> getLinksBefore() {
        return linksBefore;
    }

    public void setLinksBefore(List<String> linksBefore) {
        this.linksBefore = linksBefore;
    }

    public List<String> getLinksIn() {
        return linksIn;
    }

    public void setLinksIn(List<String> linksIn) {
        this.linksIn = linksIn;
    }

    public List<String> getLinksAfter() {
        return linksAfter;
    }

    public void setLinksAfter(List<String> linksAfter) {
        this.linksAfter = linksAfter;
    }
}
