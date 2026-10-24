package ru.yandex.practicum.logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public final class Logger implements AutoCloseable {

    private static final String FIELD_SEPARATOR = " ";
    private static final String KEY_VALUE_SEPARATOR = "=";

    private final PrintWriter out;
    private boolean closed;
    private final boolean debugMode;

    public Logger(final Path path) throws IOException {
        this(path, false);
    }

    public Logger(final Path path, final boolean debugMode) throws IOException {
        Objects.requireNonNull(path, "Log path must not be null");

        this.debugMode = debugMode;
        this.out = new PrintWriter(
                Files.newBufferedWriter(
                        path,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                )
        );
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
        ensureOpen();
        return new LogEvent(this, level);
    }

    void write(final LogEvent event) {
        Objects.requireNonNull(event, "Log event must not be null");
        ensureOpen();

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
                    "Failed to write log event"
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

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "Logger is already closed"
            );
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;
        out.close();

        if (out.checkError()) {
            throw new IllegalStateException(
                    "Failed to close logger"
            );
        }
    }
}
