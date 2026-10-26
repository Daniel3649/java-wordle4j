package ru.yandex.practicum.logger;

public enum LogField {
    TIME("_time"),
    LEVEL("level"),
    MESSAGE("_msg");

    private final String text;

    LogField(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
