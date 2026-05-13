package org.example;

public class GaussFun implements MembershipFunction {
    private double hMaxX;
    private double width;

    public double gethMaxX() {
        return hMaxX;
    }

    public void sethMaxX(double hMaxX) {
        this.hMaxX = hMaxX;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public GaussFun(double hMaxX, double width) {
        if (width <= 0) {
            throw new IllegalArgumentException();
        }
        this.hMaxX = hMaxX;
        this.width = width;
    }

    @Override
    public double getMembership(double x) {
        double membershipValue = Math.exp(-0.5 * Math.pow((x - hMaxX) / width, 2));
        if (membershipValue < 0.01) {
            return 0.0;
        }
        return membershipValue;
    }
}