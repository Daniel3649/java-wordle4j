package ru.yandex.practicum.exception;

public final class InvalidWordLengthException extends GameException {

    public InvalidWordLengthException(final int expectedLength) {
        super("Введите слово из " + expectedLength + " букв.");
    }
}
