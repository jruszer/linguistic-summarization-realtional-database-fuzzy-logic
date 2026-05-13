package org.example;


public class AbsoluteQuantifier extends Quantifier {

    public AbsoluteQuantifier(String name, FuzzySet<Double> fuzzySetRepresentation) {
        super(name, fuzzySetRepresentation);
        if (fuzzySetRepresentation.getUoD().isDiscrete()) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public double calculateT(double value) {
        return fuzzySet.getMembershipX(value);
    }

}