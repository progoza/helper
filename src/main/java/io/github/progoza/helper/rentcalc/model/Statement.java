package io.github.progoza.helper.rentcalc.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Statement {
    private Apartament apartament;
    private List<FixedCost> fixedCosts;
    private List<MeteredCost> meteredCosts;
    private List<Meter> meters;
    private LocalDate creationDate;
    private BigDecimal dueAmount = BigDecimal.ZERO;
    private LocalDate lastStatementDate;
    private int numberOfFuturePayments;
    private List<String> freeComments;

    private String frontPageTitle;
    private int countOfMonthsCovered;
    
    private BigDecimal fixedCostsPerMonth;
    private BigDecimal fixedCostsAmount;

    private BigDecimal anticipatedMeteredCostsPerMonth;
    private BigDecimal anticipatedMeteredCostsAmount;  
    private BigDecimal actualMeteredCostsAmount;

    private BigDecimal totalAnticipatedCostsPerMonth;
    private BigDecimal totalAnticipatedCosts;
    private BigDecimal totalActualCosts;

    private BigDecimal diffToPayOrReturn;
    private String diffDescription;
    
    private BigDecimal futureFixedCostPerMonth;
    private BigDecimal futureMeteredCostsPerMonth;
    private BigDecimal futureTotalCostsPerMonth;
    private List<BigDecimal> futurePayments;
    private List<LocalDate> futurePaymentsDeadline;
    private String nextMonthName;
    private String nextPeriodTitle;

    public Statement() {
        fixedCosts = new ArrayList<>();
        meteredCosts = new ArrayList<>();
        meters = new ArrayList<>();
        freeComments = new ArrayList<>();
    }
}
