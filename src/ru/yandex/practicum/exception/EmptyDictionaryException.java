package ru.yandex.practicum.exception;

public final class EmptyDictionaryException extends WordleSystemException {

    public EmptyDictionaryException() {
        super("Словарь должен содержать хотя бы одно допустимое слово из пяти букв.");
    }
}
