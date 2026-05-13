package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
public class Labelss<T extends Comparable<T>> {
    private Map<String, FuzzySet<T>> names;

    public Labelss() {
        this.names = new HashMap<>();
    }

    public void addLabel(String labelName, FuzzySet<T> fuzzySet) {
        names.put(labelName, fuzzySet);
    }

    public FuzzySet<T> getLabel(String name) {
        return names.get(name);
    }

    public Set<String> getLabelNames() {
        return names.keySet();
    }

}