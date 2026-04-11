package org.example.digitalisidomero.model;

public enum Category {
    WORK("Munka"),
    STUDY("Tanulás"),
    ENTERTAINMENT("Szórakozás"),
        OTHER("Egyéb");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}