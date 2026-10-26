package ru.yandex.practicum.logger;

public enum LogLevel {
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR"),
    DEBUG("DEBUG");

    private final String text;

    LogLevel(final String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
