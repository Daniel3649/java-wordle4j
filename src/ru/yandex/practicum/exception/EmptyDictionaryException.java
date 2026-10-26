package ru.yandex.practicum.exception;

public final class EmptyDictionaryException extends WordleSystemException {

    public EmptyDictionaryException() {
        super("Dictionary must contain at least one valid five-letter word");
    }
}
