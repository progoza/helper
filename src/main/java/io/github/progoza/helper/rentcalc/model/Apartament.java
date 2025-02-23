package io.github.progoza.helper.rentcalc.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Apartament {
    private String address;
    private BigDecimal rentAmount;
}
