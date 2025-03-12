package io.github.progoza.helper.rentcalc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.progoza.helper.rentcalc.model.FixedCost;
import io.github.progoza.helper.rentcalc.model.Meter;
import io.github.progoza.helper.rentcalc.model.MeteredCost;
import io.github.progoza.helper.rentcalc.model.Statement;

public class Calculator {

    private Statement oldStatement;
    private Statement newStatement = new Statement();

    public Statement getOldStatement() {
        return oldStatement;
    }

    public void setOldStatement(Statement oldStatement) {
        this.oldStatement = oldStatement;
    }

    public Statement getNewStatement() {
        return newStatement;
    }

    private static String[] MIESIACE = new String[]
        { "styczeń", "luty", "marzec", "kwiecień", "maj", "czerwiec", 
          "lipiec", "sierpień", "wrzesień", "październik", "listopad", "grudzień" };
    
    public static List<String> getMonthNames(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date should be before end date.");
        }

        startDate = startDate.withDayOfMonth(1).plusMonths(1);
        YearMonth current = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);

        List<String> result = new ArrayList<>(); 
        while (!current.isAfter(end)) {
            String monthName = MIESIACE [ current.getMonth().getValue() - 1 ];
            int year = current.getYear();
            result.add(monthName + " " + year);
            current = current.plusMonths(1);
        }
        return result;
    }

    private BigDecimal sumAllFixedCosts(Statement statement) {
        BigDecimal result = BigDecimal.ZERO;
        for (FixedCost cost : statement.getFixedCosts()) {
            if (!cost.isRemoved()) {
                result = result.add(cost.getAmount());
            }
        }
        return result;
    }

    private void updateUsageOfMeter(Meter meter) {
        Meter oldMeter = oldStatement.getMeters().stream().filter(x -> x.getId() == meter.getId()).findFirst().orElse(null);
        if (oldMeter == null) {
            meter.setUsage(BigDecimal.ZERO);
        } else {
            meter.setOldMeterReading(oldMeter.getReading());
            BigDecimal usage = meter.getReading().subtract(oldMeter.getReading());
            meter.setUsage(usage);
        }
    }

    private BigDecimal sumAllAnticipatedMeteredCosts(Statement statement) {
        BigDecimal anticipatedMeteredCostsPerMonth = BigDecimal.ZERO;
        for (MeteredCost meteredCost : statement.getMeteredCosts()) {
            if (meteredCost.isRemoved()) {
                continue;
            }
            BigDecimal anticipatedMeteredCost = meteredCost.getAnticipatedUsage().multiply(meteredCost.getPayPerUnit()).setScale(2, RoundingMode.HALF_EVEN);
            if (statement == oldStatement ) {
                MeteredCost correspondingMeteredCost = 
                    newStatement.getMeteredCosts().stream().filter(x -> x.getId() == meteredCost.getId()).findFirst().orElse(null);
                if (correspondingMeteredCost != null) {
                    correspondingMeteredCost.setAnticipatedCost(anticipatedMeteredCost);
                }
            } else {
                meteredCost.setFutureAnticipatedCost(anticipatedMeteredCost);
            }
            anticipatedMeteredCostsPerMonth = anticipatedMeteredCostsPerMonth.add(anticipatedMeteredCost);
        }
        return anticipatedMeteredCostsPerMonth;
    }

    private BigDecimal sumAllMetersUsageForGivenMeteredCosts(MeteredCost meteredCost) {
        List<BigDecimal> metersUsages = newStatement.getMeters().stream()
                        .filter(x -> x.getMeteredCosts().contains(meteredCost.getId()))
                        .map(x -> x.getUsage())
                        .collect(Collectors.toList());
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal usage : metersUsages) {
            result = result.add(usage);
        }
        return result;
    }

    private BigDecimal sumActualMeteredCosts() {
        BigDecimal result = BigDecimal.ZERO;
        for (MeteredCost meteredCost : oldStatement.getMeteredCosts()) {
            if (meteredCost.isRemoved()) {
                continue;
            }
            BigDecimal actualUsageForCost = sumAllMetersUsageForGivenMeteredCosts(meteredCost);
            MeteredCost correspondingMeteredCost = newStatement.getMeteredCosts().stream().filter(x -> x.getId() == meteredCost.getId()).findFirst().orElse(null);
            if (correspondingMeteredCost == null) {
                continue;
            }
            correspondingMeteredCost.setActualUsage(actualUsageForCost);
            BigDecimal actualCost = actualUsageForCost.multiply(meteredCost.getPayPerUnit()).setScale(2, RoundingMode.HALF_EVEN);
            correspondingMeteredCost.setActualCost(actualCost);
            result = result.add(actualCost);
        }
        return result;
    }

    private String getTitleCaption(List<String> months) {
        if (months.size() == 1) {
            return months.getFirst();
        } else if (months.size() > 1) {
            return months.getFirst() + " - " + months.getLast();
        } else {
            throw new IllegalArgumentException("Statement to be generated for the same period?");
        }
    }

    private String getDiffDescription(BigDecimal diff) {
        return diff.compareTo(BigDecimal.ZERO) <= 0 ? "Nadpłata" : "Niedopłata";
    }

    public void calculate() {
        List<String> months = getMonthNames(oldStatement.getCreationDate(), newStatement.getCreationDate());

        newStatement.setCountOfMonthsCovered(months.size());
        newStatement.setFrontPageTitle(getTitleCaption(months));
        newStatement.setLastStatementDate(oldStatement.getCreationDate());

        for (Meter meter : newStatement.getMeters()) {
            updateUsageOfMeter(meter);
        }

        BigDecimal fixedCostPerMonth = sumAllFixedCosts(oldStatement);
        BigDecimal fixedCostsAmount = fixedCostPerMonth.multiply(BigDecimal.valueOf(months.size())).setScale(2, RoundingMode.HALF_EVEN);
        newStatement.setFixedCostsPerMonth(fixedCostPerMonth);
        newStatement.setFixedCostsAmount(fixedCostsAmount);

        BigDecimal anticipatedMeteredCostsPerMonth = sumAllAnticipatedMeteredCosts(oldStatement);
        BigDecimal anticipatedMeteredCostsAmount = anticipatedMeteredCostsPerMonth.multiply(BigDecimal.valueOf(months.size())).setScale(2, RoundingMode.HALF_EVEN);
        newStatement.setAnticipatedMeteredCostsPerMonth(anticipatedMeteredCostsPerMonth);;
        newStatement.setAnticipatedMeteredCostsAmount(anticipatedMeteredCostsAmount);

        BigDecimal actualMeteredCostsAmount = sumActualMeteredCosts();
        newStatement.setActualMeteredCostsAmount(actualMeteredCostsAmount);

        newStatement.setTotalAnticipatedCosts(anticipatedMeteredCostsAmount.add(fixedCostsAmount));
        newStatement.setTotalAnticipatedCostsPerMonth(anticipatedMeteredCostsPerMonth.add(fixedCostPerMonth));
        newStatement.setTotalActualCosts(actualMeteredCostsAmount.add(fixedCostsAmount));

        BigDecimal diff = newStatement.getTotalActualCosts().subtract(newStatement.getTotalAnticipatedCosts());
        newStatement.setDiffToPayOrReturn(diff);
        newStatement.setDiffDescription(getDiffDescription(diff));

        BigDecimal futureFixedCostPerMonth = sumAllFixedCosts(newStatement);
        newStatement.setFutureFixedCostPerMonth(futureFixedCostPerMonth);
        BigDecimal futureMeteredCostsPerMonth = sumAllAnticipatedMeteredCosts(newStatement);
        newStatement.setFutureMeteredCostsPerMonth(futureMeteredCostsPerMonth);       
        newStatement.setFutureTotalCostsPerMonth(futureMeteredCostsPerMonth.add(futureFixedCostPerMonth));

        List<BigDecimal> futurePayments = new ArrayList<>();
        newStatement.setFuturePayments(futurePayments);
        List<LocalDate> futurePaymentsDeadlines = new ArrayList<>();
        newStatement.setFuturePaymentsDeadline(futurePaymentsDeadlines);
        for (int i=0; i< newStatement.getNumberOfFuturePayments(); i++) {
            BigDecimal futurePayment = newStatement.getApartament().getRentAmount().add(newStatement.getFutureTotalCostsPerMonth());
            if (i==0) {
                futurePayment = futurePayment.add(diff);
                newStatement.setNextMonthName(MIESIACE[newStatement.getCreationDate().plusMonths(1).getMonthValue() - 1]);
            }
            futurePayments.add(futurePayment);
            LocalDate futureDeadline = newStatement.getCreationDate().withDayOfMonth(10).plusMonths(1 + i);
            futurePaymentsDeadlines.add(futureDeadline);
            List<String> futureMonths = getMonthNames(futurePaymentsDeadlines.getFirst().minusMonths(1), futurePaymentsDeadlines.getLast());
            newStatement.setNextPeriodTitle(getTitleCaption(futureMonths));
        }
    }
}