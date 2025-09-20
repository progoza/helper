package io.github.progoza.helper.carmaint.model;

import java.util.List;

import lombok.Data;

@Data
public class InspectionBook {
    private String carName;
    private List<InspectionItem> inspectionItems;
}
