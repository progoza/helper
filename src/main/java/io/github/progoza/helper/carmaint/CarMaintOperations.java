package io.github.progoza.helper.carmaint;

import io.github.progoza.helper.carmaint.model.*;

public interface CarMaintOperations {
    InspectionBook getInspectionBook(String carName);
    void getNearestInspectionRecommendation(String carName);
}