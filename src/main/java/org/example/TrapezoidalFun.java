package org.example;

public class TrapezoidalFun implements MembershipFunction {
    private double start_up, end_up, start_down, end_down;

    public TrapezoidalFun(double start_up, double end_up, double start_down, double end_down) {
        this.start_up = start_up;
        this.end_up = end_up;
        this.start_down = start_down;
        this.end_down = end_down;
    }

    public double getStart_up() {
        return start_up;
    }

    public void setStart_up(double start_up) {
        this.start_up = start_up;
    }

    public double getEnd_up() {
        return end_up;
    }

    public void setEnd_up(double end_up) {
        this.end_up = end_up;
    }

    public double getStart_down() {
        return start_down;
    }

    public void setStart_down(double start_down) {
        this.start_down = start_down;
    }

    public double getEnd_down() {
        return end_down;
    }

    public void setEnd_down(double end_down) {
        this.end_down = end_down;
    }

    @Override
    public double getMembership(double x) {
        if (x < start_up || x > end_down) {
            return 0.0;
        } else if (x >= end_up && x <= start_down) {
            return 1.0;
        } else if (x >= start_up && x < end_up) {
            if (end_up == start_up) {
                return 1.0;
            }
            return (x - start_up) / (end_up - start_up);
        } else {
            if (end_down == start_down) {
                return 1.0;
            }
            return (end_down - x) / (end_down - start_down);
        }
    }
}