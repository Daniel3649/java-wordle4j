package ru.yandex.practicum.exception;

public abstract class GameException extends Exception {

    protected GameException(final String message) {
        super(message);
    }
}
