package io.github.progoza.helper.carmaint.model;

import java.util.List;

import lombok.Data;

@Data
public class Inspection {
    private List<InspectionItem> items;
}
