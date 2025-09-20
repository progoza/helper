package io.github.progoza.helper.carmaint.model;

import lombok.Data;

@Data
public class InspectionRecommendationItem {
    private String name;
    private int inspectionIntervalKm;
    private int inspectionIntervalMonths;
}