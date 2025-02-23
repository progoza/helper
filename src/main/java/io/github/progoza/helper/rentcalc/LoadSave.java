package io.github.progoza.helper.rentcalc;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.progoza.helper.rentcalc.model.Statement;

public class LoadSave {
    private String filePrefix;

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    private ObjectMapper mapper = new ObjectMapper();
    {
        mapper.findAndRegisterModules();
    } 

    public void saveStatement(Statement s, String dir) {
        if (filePrefix.isBlank()) {
            throw new IllegalStateException("Attempt to save statement without assigning the file prefix.");
        }
        if (dir.isBlank()) {
            throw new IllegalArgumentException("Attempt to save statement without providing directory");
        }
        if (dir.endsWith("/")) {
            dir = dir.substring(0, dir.length() - 1);
        }

        String fileName = dir + "/" + filePrefix + "-" + s.getCreationDate().getYear() + "-" + s.getCreationDate().getMonthValue() + ".json";    
        
        try {
            File file=new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }  
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, s);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write statement to file", e);
        }
    }

    public Statement loadStatement(String dir, String fileName) {
        if (!fileName.contains("-")) {
            throw new IllegalArgumentException("Statement file name must contain '-' character");
        }
        if (dir.isBlank()) {
            throw new IllegalArgumentException("Attempt to load statement without providing directory");
        }
        if (dir.endsWith("/")) {
            dir = dir.substring(0, dir.length() - 1);
        }

        try {
            filePrefix = fileName.split("-")[0];
            return mapper.readValue(new File(dir + "/" + fileName), Statement.class);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load statement from file", e);
        }
    }

    private static Comparator<String> statementFileComparator = new Comparator<String>() {
        @Override
        public int compare(String arg0, String arg1) {
            String s0 = arg0.substring(arg0.indexOf('-'));
            String s1 = arg1.substring(arg1.indexOf('-'));
            return s1.compareTo(s0);
        }
    };

    public List<String> listStaments(String dir) {
        return Stream.of(new File(dir).listFiles())
            .filter(file -> !file.isDirectory() && file.getName().endsWith(".json"))
            .map(File::getName)
            .sorted(statementFileComparator)
            .collect(Collectors.toList());
    }
}
