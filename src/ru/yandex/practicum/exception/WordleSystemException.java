package ru.yandex.practicum.exception;

public abstract class WordleSystemException extends Exception {

    protected WordleSystemException(final String message) {
        super(message);
    }

    protected WordleSystemException(
            final String message,
            final Throwable cause
    ) {
        super(message, cause);
    }
}
