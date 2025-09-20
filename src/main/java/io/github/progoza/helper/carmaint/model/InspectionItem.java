package io.github.progoza.helper.carmaint.model;

import java.util.Date;

import lombok.Data;

@Data
public class InspectionItem {
    private InspectionRecommendationItem recommendation;
    private int inspectionKm;
    private Date inspectionDate;
}
