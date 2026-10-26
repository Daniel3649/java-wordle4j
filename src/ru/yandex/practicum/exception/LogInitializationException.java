package ru.yandex.practicum.exception;

import java.nio.file.Path;

public final class LogInitializationException extends WordleSystemException {

    public LogInitializationException(final Path path, final Throwable cause) {
        super("Failed to create log file: " + path, cause);
    }
}
