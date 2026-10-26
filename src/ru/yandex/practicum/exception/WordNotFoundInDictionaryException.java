package ru.yandex.practicum.exception;

public final class WordNotFoundInDictionaryException extends GameException {

    public WordNotFoundInDictionaryException(final String word) {
        super("Слова «" + word + "» нет в словаре. Введите другое слово.");
    }
}
