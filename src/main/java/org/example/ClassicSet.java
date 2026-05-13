package org.example;

import java.util.HashSet;
import java.util.Set;

public class ClassicSet<T extends Comparable<T>> {
    private Set<T> elements;
    private UniverseOfDiscourse<T> UoD;

    public ClassicSet(UniverseOfDiscourse<T> UoD) {
        this.elements = new HashSet<>();
        this.UoD = UoD;
    }

//    public ClassicSet(UniverseOfDiscourse<T> universe, Set<T> elements) {
//        this(universe);
//        elements.forEach(this::addElement);
//    }

    public void addElement(T element) {
        if (!UoD.contains(element)) {
            return;
        }
        elements.add(element);
    }


    public boolean contains(T element) {
        return this.elements.contains(element);
    }

    public Set<T> getElements() {
        return new HashSet<>(this.elements);
    }

    public UniverseOfDiscourse<T> getUoD() {
        return UoD;
    }

    public ClassicSet<T> complement() {
        if (!UoD.isDiscrete()) {
            throw new UnsupportedOperationException();
        }
        ClassicSet<T> complementSet = new ClassicSet<>(UoD);
        for (T element : UoD.getElements()) {
            if (!this.UoD.getElements().contains(element)) {
                complementSet.addElement(element);
            }
        }
        return complementSet;
    }

    public ClassicSet<T> union(ClassicSet<T> set2) {
        if (!this.UoD.equals(set2.UoD)) {
            throw new IllegalArgumentException();
        }
        ClassicSet<T> unionSet = new ClassicSet<>(UoD);
        unionSet.elements.addAll(this.elements);
        unionSet.elements.addAll(set2.elements);
        return unionSet;
    }

    public ClassicSet<T> intersection(ClassicSet<T> set2) {
        ClassicSet<T> intersectionSet = new ClassicSet<>(UoD);
        for (T element : this.elements) {
            if (set2.elements.contains(element)) {
                intersectionSet.elements.add(element);
            }
        }
        return intersectionSet;
    }

}