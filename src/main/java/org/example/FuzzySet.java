package org.example;

import java.util.*;

public class FuzzySet<T extends Comparable<T>> {
    private Map<T, Double> memberships;
    private UniverseOfDiscourse<T> UoD;
    private MembershipFunction membershipFunction;

    //dyskr
    public FuzzySet(UniverseOfDiscourse<T> UoD) {
        if (!UoD.isDiscrete()) {
            throw new IllegalArgumentException();
        }
        this.UoD = UoD;
        this.memberships = new HashMap<>();
        for (T element : UoD.getElements()) {
            this.memberships.put(element, 0.0);
        }
    }

    //dyskr + wart
    public FuzzySet(UniverseOfDiscourse<T> UoD, Map<T, Double> initialMemberships) {
        this(UoD);
        initialMemberships.forEach((element, degree) -> {
            if (!UoD.contains(element)) {
                throw new IllegalArgumentException();
            }
            if (degree < 0.0 || degree > 1.0) {
                throw new IllegalArgumentException();
            }
            this.memberships.put(element, degree);
        });
    }

    //gesta
    public FuzzySet(UniverseOfDiscourse<T> UoD, MembershipFunction membershipFunction) {
        if (UoD.isDiscrete()) {
            throw new IllegalArgumentException();
        }
        this.UoD = UoD;
        this.membershipFunction = membershipFunction;
    }

    public UniverseOfDiscourse<T> getUoD() {
        return UoD;
    }

    //dsykr
    public double getMembership(T element) {
        if (!UoD.isDiscrete()) {
            throw new UnsupportedOperationException();
        }
        return memberships.getOrDefault(element, 0.0);
    }

    //gesta
    public double getMembershipX(double x) {
        if (UoD.isDiscrete()) {
            throw new UnsupportedOperationException();
        }
        if (membershipFunction == null) {
            throw new IllegalStateException();
        }
        return membershipFunction.getMembership(x);
    }

    public void setMembership(T element, double degree) {
        if (!UoD.isDiscrete()) {
            throw new UnsupportedOperationException();
        }
        if (!UoD.contains(element) || (degree < 0 || degree > 1)) {
            throw new IllegalArgumentException();
        }
        memberships.put(element, degree);
    }


    public double getCard() {
        if (UoD.isDiscrete()) {
            double sumDegrees = 0.0;
            for (double degree : memberships.values()) {
                sumDegrees += degree;
            }
            return sumDegrees;
        } else {
            double dx = 0.01;

            Set<Double> samples = getSamples(0.01);

            double sumMembershipValues = 0.0;
            for (Double point : samples) {
                sumMembershipValues += this.getMembershipX(point);
            }
            return sumMembershipValues * dx;
        }
    }


    public boolean isEmpty() {
        if (UoD.isDiscrete()) {
            return memberships.values().stream().allMatch(d -> d == 0.0);
        } else {
            Set<Double> samples = getSamples(0.01);
            for (Double point : samples) {
                if (getMembershipX(point) != 0.0) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean isConvex() {
        if (!UoD.isDiscrete()) {
            //tutaj dodac to z alfa przekrojem
            return true;
        }

        if (UoD.getElements().isEmpty()) {
            return true;
        }

        return true;
    }

    public boolean isNormal() {
        if (UoD.isDiscrete()) {
            return memberships.values().stream().anyMatch(d -> d == 1.0);
        } else {
            if (membershipFunction instanceof TriangularFun) {
                return (membershipFunction).getMembership(((TriangularFun) membershipFunction).getPeak()) == 1.0;
            } else if (membershipFunction instanceof TrapezoidalFun) {
                return (membershipFunction).getMembership(((TrapezoidalFun) membershipFunction).getEnd_up()) == 1.0;
            } else if (membershipFunction instanceof GaussFun) {
                return (membershipFunction).getMembership(((GaussFun) membershipFunction).gethMaxX()) == 1.0;
            }
            return false;
        }
    }

    public double getHeight() {
        if (UoD.isDiscrete()) {
            double maxHeight = 0.0;
            for (double degree : memberships.values()) {
                if (degree > maxHeight) {
                    maxHeight = degree;
                }
            }
            return maxHeight;
        } else {
            if (membershipFunction instanceof TriangularFun) {
                return (membershipFunction).getMembership(((TriangularFun) membershipFunction).getPeak());
            } else if (membershipFunction instanceof TrapezoidalFun) {
                return (membershipFunction).getMembership(((TrapezoidalFun) membershipFunction).getEnd_up());
            } else if (membershipFunction instanceof GaussFun) {
                return (membershipFunction).getMembership(((GaussFun) membershipFunction).gethMaxX());
            }
            return 0.0;
        }
    }

    public ClassicSet<T> getSupp() {
        ClassicSet<T> supportSet = new ClassicSet<>(UoD);

        if (UoD.isDiscrete()) {
            memberships.forEach((element, degree) -> {
                if (degree > 0.0) {
                    supportSet.addElement(element);
                }
            });
        } else {
            Set<Double> samples = getSamples(0.01);
            for (Double point : samples) {
                if (getMembershipX(point) > 0.0) {
                    supportSet.addElement((T) point);
                }
            }
        }
        return supportSet;
    }

    public ClassicSet<T> getAlphaCut(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException();
        }

        ClassicSet<T> alphaCutSet = new ClassicSet<>(UoD);

        if (UoD.isDiscrete()) {
            memberships.forEach((element, degree) -> {
                if (degree >= alpha) {
                    alphaCutSet.addElement(element);
                }
            });
        } else {
            Set<Double> samples = getSamples(0.01);
            for (Double point : samples) {
                if (getMembershipX(point) >= alpha) {
                    alphaCutSet.addElement((T) point);
                }
            }
        }
        return alphaCutSet;
    }


    public FuzzySet<T> complement() {
        if (UoD.isDiscrete()) {
            Map<T, Double> complementedMemberships = new HashMap<>();
            memberships.forEach((element, degree) -> complementedMemberships.put(element, 1.0 - degree));
            return new FuzzySet<>(UoD, complementedMemberships);
        } else {
            return new FuzzySet<>(UoD, x -> 1.0 - membershipFunction.getMembership(x));
        }
    }


    public FuzzySet<T> union(FuzzySet<T> set2) {
        if (UoD.isDiscrete()) {
            Map<T, Double> unionMemberships = new HashMap<>();
            for (T element : UoD.getElements()) {
                double degreeA = this.getMembership(element);
                double degreeB = set2.getMembership(element);
                unionMemberships.put(element, Math.max(degreeA, degreeB));
            }
            return new FuzzySet<>(UoD, unionMemberships);
        } else {
            return new FuzzySet<>(UoD, x -> Math.max(this.getMembershipX(x), set2.getMembershipX(x)));
        }
    }

    public FuzzySet<T> intersection(FuzzySet<T> set2) {
        if (UoD.isDiscrete()) {
            Map<T, Double> intersectionMemberships = new HashMap<>();
            for (T element : UoD.getElements()) {
                double degreeA = this.getMembership(element);
                double degreeB = set2.getMembership(element);
                intersectionMemberships.put(element, Math.min(degreeA, degreeB));
            }
            return new FuzzySet<>(UoD, intersectionMemberships);
        } else {
            return new FuzzySet<>(UoD, x -> Math.min(this.getMembershipX(x), set2.getMembershipX(x)));
        }
    }

    public double getDegOfFuzz() {
        double suppSize;
        double XSize;

        if (UoD.isDiscrete()) {
            suppSize = this.getSupp().getElements().size();
            XSize = UoD.getElements().size();
        } else {
            Set<Double> samples = getSamples(0.01);

            int counter = 0;
            for (Double point : samples) {
                if (getMembershipX(point) > 0.0) {
                    counter++;
                }
            }
            suppSize = counter;

            XSize = samples.size();
        }

        if (XSize == 0) {
            return -1.0;
        }

        return suppSize / XSize;
    }

    public Set<Double> getSamples(double step) {
        Set<Double> samples = new TreeSet<>();

        for (double n = getUoD().getMinX(); n <= getUoD().getMaxX() + step; n += step) {
            samples.add(n);
        }

        return samples;
    }
}