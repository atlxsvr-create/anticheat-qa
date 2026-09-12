package dev.anticheatqa.module;

public enum Category {
    VISUALS("Visuals"),
    MONITOR("Monitor"),
    QA("QA"),
    SETTINGS("Settings");

    public final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }
}
