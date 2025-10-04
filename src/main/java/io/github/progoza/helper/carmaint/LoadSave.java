package io.github.progoza.helper.carmaint;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.progoza.helper.carmaint.model.Car;

public class LoadSave {

    public void saveCarData(Car car) {
        // Implementation for saving car data
    }

    public Car loadCarData(String dir, String fileName) {
        // Implementation for loading car data
        return new Car();
    }

    public List<String> listCars(String dir) {
        return Stream.of(new File(dir).listFiles())
            .filter(file -> !file.isDirectory() && file.getName().endsWith(".json"))
            .map(File::getName)
            .collect(Collectors.toList());
    }
}
