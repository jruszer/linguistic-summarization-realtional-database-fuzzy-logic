package org.example;

import java.util.Set;

public class LinguisticVariable<T extends Comparable<T>> {
    private String name;
    private UniverseOfDiscourse<T> UoD;
    private Labelss<T> labels;

    public LinguisticVariable(String name, UniverseOfDiscourse<T> UoD) {
        this.name = name;
        this.UoD = UoD;
        this.labels = new Labelss<>();
    }

    public String getName() {
        return name;
    }

    public UniverseOfDiscourse<T> getUoD() {
        return UoD;
    }

    public void addLabel(String labelName, FuzzySet<T> fuzzySet) {
        if (!fuzzySet.getUoD().equals(this.UoD)) { //to przy dodawaniu nowej bedzie wazen
            throw new IllegalArgumentException();
        }
        labels.addLabel(labelName, fuzzySet);
    }


    public FuzzySet<T> getLabel(String labelName) {
        return labels.getLabel(labelName);
    }

    public double getMembership(T element, String labelName) {
        FuzzySet<T> fuzzySet = labels.getLabel(labelName);
        if (fuzzySet == null) {
            throw new RuntimeException();
        }
        if (UoD.isDiscrete()) {
            return fuzzySet.getMembership(element);
        } else {
            return fuzzySet.getMembershipX(((Number) element).doubleValue());
        }
    }

    public Set<String> getLabelNames() {
        return labels.getLabelNames();
    }

}