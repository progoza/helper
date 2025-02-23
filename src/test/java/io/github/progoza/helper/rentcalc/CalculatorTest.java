package io.github.progoza.helper.rentcalc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import io.github.progoza.helper.rentcalc.model.FixedCost;
import io.github.progoza.helper.rentcalc.model.Meter;
import io.github.progoza.helper.rentcalc.model.MeteredCost;
import io.github.progoza.helper.rentcalc.model.Statement;

public class CalculatorTest {

    @Test
    public void monthsBetweenTwoDates() {
        LocalDate startDate = LocalDate.of(2024, 11, 30);
        LocalDate endDate = LocalDate.of(2025, 2, 28);

        List<String> result = Calculator.getMonthNames(startDate, endDate);

        assertThat(result).hasSize(3).contains("grudzień 2024", "styczeń 2025", "luty 2025");     
    }

    @Test 
    public void testSummingOfFixedCosts() {
        Calculator testObj = new Calculator();

        FixedCost ofc1 = new FixedCost();
        ofc1.setId(1);
        ofc1.setPayAmount(new BigDecimal("3123.45"));
        FixedCost ofc2 = new FixedCost();
        ofc2.setId(2);
        ofc2.setPayAmount(new BigDecimal("3234.32"));
        testObj.setOldStatement(new Statement());
        testObj.getOldStatement().getFixedCosts().add(ofc1);
        testObj.getOldStatement().getFixedCosts().add(ofc2);
        testObj.getOldStatement().setCreationDate(LocalDate.of(2024, 10, 31));
        FixedCost nfc1 = new FixedCost();
        nfc1.setId(1);
        nfc1.setPayAmount(new BigDecimal("123.45"));
        FixedCost nfc2 = new FixedCost();
        nfc2.setId(2);
        nfc2.setPayAmount(new BigDecimal("234.32"));
        testObj.getNewStatement().getFixedCosts().add(nfc1);
        testObj.getNewStatement().getFixedCosts().add(nfc2);
        testObj.getNewStatement().setCreationDate(LocalDate.of(2024, 12, 31));

        testObj.calculate();

        assertThat(testObj.getNewStatement().getTotalAnticipatedCosts())
            .isCloseTo(new BigDecimal((3123.45+3234.32)*2), Offset.offset(new BigDecimal("0.001")));

        assertThat(testObj.getNewStatement().getTotalActualCosts())
            .isCloseTo(new BigDecimal((3123.45+3234.32)*2), Offset.offset(new BigDecimal("0.001")));

        assertThat(testObj.getNewStatement().getFixedCostsAmount())
                .isCloseTo(new BigDecimal((3123.45+3234.32)*2), Offset.offset(new BigDecimal("0.001")));

        assertThat(testObj.getNewStatement().getFixedCostsPerMonth())
                .isCloseTo(new BigDecimal(3123.45+3234.32), Offset.offset(new BigDecimal("0.001")));
    
        assertThat(testObj.getNewStatement().getActualMeteredCostsAmount()).isEqualTo(BigDecimal.ZERO);
    }


    @Test 
    public void testSummingOfMeteredCosts() {
        Calculator testObj = new Calculator();

        MeteredCost omc1 = new MeteredCost();
        omc1.setId(1L);
        omc1.setAnticipatedUsage(new BigDecimal("1.2"));
        omc1.setPayPerUnit(new BigDecimal("2.1"));

        MeteredCost omc2 = new MeteredCost();
        omc2.setId(2L);
        omc2.setAnticipatedUsage(new BigDecimal("3.2"));
        omc2.setPayPerUnit(new BigDecimal("4.1"));

        testObj.setOldStatement(new Statement());
        testObj.getOldStatement().getMeteredCosts().add(omc1);
        testObj.getOldStatement().getMeteredCosts().add(omc2);
        testObj.getOldStatement().setCreationDate(LocalDate.of(2024, 10, 31));
    
        MeteredCost nmc1 = new MeteredCost();
        nmc1.setId(1L);
        nmc1.setAnticipatedUsage(new BigDecimal("11.2"));
        nmc1.setPayPerUnit(new BigDecimal("12.1"));

        MeteredCost nmc2 = new MeteredCost();
        nmc2.setId(2L);
        nmc2.setAnticipatedUsage(new BigDecimal("13.2"));
        nmc2.setPayPerUnit(new BigDecimal("14.1"));

        testObj.getNewStatement().getMeteredCosts().add(nmc1);
        testObj.getNewStatement().getMeteredCosts().add(nmc2);
        testObj.getNewStatement().setCreationDate(LocalDate.of(2024, 12, 31));

        Meter oldMeter = new Meter();
        oldMeter.setId(1L);
        oldMeter.getMeteredCosts().add(1L);
        oldMeter.getMeteredCosts().add(2L);
        oldMeter.setReading(new BigDecimal("12.5"));
        testObj.getOldStatement().getMeters().add(oldMeter);

        Meter newMeter = new Meter();
        newMeter.setId(1L);
        newMeter.getMeteredCosts().add(1L);
        newMeter.getMeteredCosts().add(2L);
        newMeter.setReading(new BigDecimal("13.6"));
        testObj.getNewStatement().getMeters().add(newMeter);

        testObj.calculate();

        assertThat(testObj.getNewStatement().getFixedCostsAmount()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
        
        assertThat(testObj.getNewStatement().getMeters().getFirst().getOldMeterReading()).isEqualTo(new BigDecimal("12.5"));
        assertThat(testObj.getNewStatement().getMeters().getFirst().getUsage()).isEqualTo(new BigDecimal("1.1"));

        assertThat(testObj.getNewStatement().getTotalAnticipatedCosts())
            .isCloseTo(new BigDecimal( (1.2*2.1 + 3.2 * 4.1)*2 ), Offset.offset(new BigDecimal("0.001")));

        assertThat(testObj.getNewStatement().getActualMeteredCostsAmount())
            .isCloseTo(new BigDecimal( 1.1* (2.1 + 4.1) ), Offset.offset(new BigDecimal("0.001")));

        assertThat(testObj.getNewStatement().getTotalActualCosts())
            .isCloseTo(new BigDecimal( (13.6-12.5)*(2.1 + 4.1)), Offset.offset(new BigDecimal("0.001")));
        }

}
