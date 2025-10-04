package io.github.progoza.helper.carmaint.model;

import lombok.Data;

@Data
public class Car {
    private String name;
    private int milageKm;
    private InspectionBook inspectionBook;
    private InspectionRecommendations inspectionRecommendations;
    private int counter;
}
