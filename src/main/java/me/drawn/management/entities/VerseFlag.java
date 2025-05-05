package me.drawn.management.entities;

public class VerseFlag {

    private final String name;
    private boolean value;

    public VerseFlag(String name, boolean value) {
        this.name = name;
        this.value = value;
    }

    public VerseFlag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean getValue() {
        return value;
    }
}
