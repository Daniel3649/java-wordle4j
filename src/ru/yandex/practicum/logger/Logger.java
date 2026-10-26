package ru.yandex.practicum.logger;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public final class Logger {

    private static final String FIELD_SEPARATOR = " ";
    private static final String KEY_VALUE_SEPARATOR = "=";

    private final PrintWriter out;
    private final boolean debugMode;

    public Logger(final PrintWriter out) {
        this(out, false);
    }

    public Logger(final PrintWriter out, final boolean debugMode) {
        this.out = Objects.requireNonNull(out, "Поток записи лога не задан.");
        this.debugMode = debugMode;
    }

    public LogEvent atInfo() {
        return newEvent(LogLevel.INFO);
    }

    public LogEvent atWarn() {
        return newEvent(LogLevel.WARN);
    }

    public LogEvent atError() {
        return newEvent(LogLevel.ERROR);
    }

    public LogEvent atDebug() {
        return newEvent(LogLevel.DEBUG);
    }

    private LogEvent newEvent(final LogLevel level) {
        return new LogEvent(this, level);
    }

    void write(final LogEvent event) {
        Objects.requireNonNull(event, "Событие лога не задано.");

        if (event.getLevel() == LogLevel.DEBUG && !debugMode) {
            return;
        }

        StringBuilder log = new StringBuilder();

        appendField(
                log,
                LogField.TIME.getText(),
                Instant.now()
        );

        appendField(
                log,
                LogField.LEVEL.getText(),
                event.getLevel().getText()
        );

        appendField(
                log,
                LogField.MESSAGE.getText(),
                event.getMessage()
        );

        for (Map.Entry<String, Object> entry
                : event.getAttributes().entrySet()) {

            appendField(
                    log,
                    entry.getKey(),
                    entry.getValue()
            );
        }

        out.println(log);

        if (out.checkError()) {
            throw new IllegalStateException(
                    "Не удалось записать событие в лог."
            );
        }
    }

    private static void appendField(
            final StringBuilder log,
            final String key,
            final Object value
    ) {
        String text = String.valueOf(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        if (!log.isEmpty()) {
            log.append(FIELD_SEPARATOR);
        }

        log.append(key)
                .append(KEY_VALUE_SEPARATOR)
                .append('"')
                .append(text)
                .append('"');
    }
}
