package org.example;

public class TriangularFun implements MembershipFunction {
    private double start_up, peak, end_down;

    public TriangularFun(double start_up, double peak, double end_down) {
        this.start_up = start_up;
        this.peak = peak;
        this.end_down = end_down;
    }

    public double getStart_up() {
        return start_up;
    }

    public void setStart_up(double start_up) {
        this.start_up = start_up;
    }

    public double getPeak() {
        return peak;
    }

    public void setPeak(double peak) {
        this.peak = peak;
    }

    public double getEnd_down() {
        return end_down;
    }

    public void setEnd_down(double end_down) {
        this.end_down = end_down;
    }

    @Override
    public double getMembership(double x) {
        if (x <= start_up || x >= end_down) {
            return 0.0;
        } else if (x >= start_up && x <= peak) {
            return (x - start_up) / (peak - start_up);
        } else if (x > peak && x <= end_down) {
            return (end_down - x) / (end_down - peak);
        }
        return 0.0;
    }
}