package org.example;

public class RelativeQuantifier extends Quantifier {

    public RelativeQuantifier(String name, FuzzySet<Double> fuzzySetRepresentation) {
        super(name, fuzzySetRepresentation);
        if (!fuzzySetRepresentation.getUoD().isDiscrete()) {
            double min = fuzzySetRepresentation.getUoD().getMinX();
            double max = fuzzySetRepresentation.getUoD().getMaxX();
            if (!(min >= 0.0 && max <= 1.0)) {
                System.err.println("zly przedzial");
            }
        }
    }

    @Override
    public double calculateT(double value) {
        return fuzzySet.getMembershipX(value);
    }

}