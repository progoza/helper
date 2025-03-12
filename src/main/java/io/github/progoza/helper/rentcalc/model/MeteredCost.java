package io.github.progoza.helper.rentcalc.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeteredCost {
    private long id;
    private String description;
    private String unitName;
    private BigDecimal payPerUnit;
    private BigDecimal anticipatedUsage;
    private boolean removed;
    private boolean changed;

    private BigDecimal anticipatedCost;
    private BigDecimal actualUsage;
    private BigDecimal actualCost;
    private BigDecimal futureAnticipatedCost;

}
