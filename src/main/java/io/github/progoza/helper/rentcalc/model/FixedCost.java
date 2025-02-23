package io.github.progoza.helper.rentcalc.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FixedCost {
    private long id;
    private String description;
    private BigDecimal payAmount;
    private boolean isChanged;
    private boolean isRemoved;
}
