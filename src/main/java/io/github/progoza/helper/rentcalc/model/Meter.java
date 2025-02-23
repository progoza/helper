package io.github.progoza.helper.rentcalc.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meter {
    private long id;
    private String name;
    private BigDecimal reading;
    private List<Long> meteredCosts;
    private boolean removed;
    private BigDecimal oldMeterReading;

    private BigDecimal usage;

    public Meter() {
        meteredCosts = new ArrayList<>();
    }
}