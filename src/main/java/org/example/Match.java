package org.example;

import java.util.HashMap;
import java.util.Map;

public class Match {
    private final Map<String, Double> numbers;
    private final Map<String, String> strings;  //tutaj data

    public Match() {
        this.numbers = new HashMap<>();
        this.strings = new HashMap<>();
    }

    public void addNumericValue(String columnName, Double value) {
        this.numbers.put(columnName, value);
    }

    public void addStringValue(String columnName, String value) {
        this.strings.put(columnName, value);
    }

    public Double getNumericValue(String columnName) {
        return numbers.get(columnName);
    }

    public String getStringValue(String columnName) {
        return strings.get(columnName);
    }

}