package ru.yandex.practicum.exception;

public final class InvalidWordCharactersException extends GameException {

    public InvalidWordCharactersException() {
        super("Слово должно содержать только русские буквы.");
    }
}
