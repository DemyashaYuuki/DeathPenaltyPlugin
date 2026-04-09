package ru.demyasha.edenreligion;

import java.util.Locale;
import java.util.Optional;

public enum Choice {
    ASUNA("asuna", "Асуна"),
    ERROR("error", "Error"),
    AGAINST_BOTH("against_both", "Против обоих"),
    NONE("none", "Не участвую в лоре");

    private final String key;
    private final String displayName;

    Choice(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<Choice> fromSerialized(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (Choice choice : values()) {
            if (choice.name().equals(normalized) || choice.key.equalsIgnoreCase(value)) {
                return Optional.of(choice);
            }
        }
        return Optional.empty();
    }
}
