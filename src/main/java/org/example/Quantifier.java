package org.example;


public abstract class Quantifier {
    protected String label;
    protected FuzzySet<Double> fuzzySet;

    public Quantifier(String label, FuzzySet<Double> fuzzySet) {
        this.label = label;
        this.fuzzySet = fuzzySet;
    }

    public String getLabel() {
        return label;
    }

    public FuzzySet<Double> getFuzzySet() {
        return fuzzySet;
    }

    public abstract double calculateT(double value);
}