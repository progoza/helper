package io.github.progoza.helper.carmaint.model;

import java.util.List;

import lombok.Data;

@Data
public class InspectionRecommendations {
    private List<InspectionRecommendationItem> recommendations;
    private String carName;
}