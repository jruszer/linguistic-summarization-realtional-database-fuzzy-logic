package org.example;

import java.util.Set;
import java.util.TreeSet;

public class UniverseOfDiscourse<T> {
    private Set<T> elements;
    private Double minX;
    private Double maxX;
    private boolean isDiscrete;

    //dyskretna
    public UniverseOfDiscourse(Set<T> elements) {
        this.elements = new TreeSet<>(elements);
        this.isDiscrete = true;
    }

    //gesta
    public UniverseOfDiscourse(double minX, double maxX) {
        if (minX >= maxX) {
            throw new IllegalArgumentException("min > max");
        }
        this.minX = minX;
        this.maxX = maxX;
        this.isDiscrete = false;
    }

    public boolean isDiscrete() {
        return isDiscrete;
    }

    public Set<T> getElements() {
        if (!isDiscrete) {
            throw new UnsupportedOperationException();
        }
        return elements;
    }

    public double getMinX() {
        if (isDiscrete) {
            throw new UnsupportedOperationException();
        }
        return minX;
    }

    public double getMaxX() {
        if (isDiscrete) {
            throw new UnsupportedOperationException();
        }
        return maxX;
    }

    public boolean contains(T element) {
        if (isDiscrete) {
            return elements.contains(element);
        } else {
            if (element instanceof Number) {
                double val = ((Number) element).doubleValue();
                return val >= minX && val <= maxX;
            }
            throw new IllegalArgumentException("zamiast return");
        }
    }

}