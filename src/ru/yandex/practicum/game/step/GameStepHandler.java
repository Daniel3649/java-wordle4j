package ru.yandex.practicum.game.step;

import ru.yandex.practicum.game.dictionary.WordleDictionary;
import ru.yandex.practicum.game.engine.WordleGame;

import java.util.Objects;

public final class GameStepHandler {

    private final WordleGame game;
    private final WordleDictionary dictionary;

    public GameStepHandler(
            final WordleGame game,
            final WordleDictionary dictionary
    ) {
        this.game = Objects.requireNonNull(
                game, "Game must not be null"
        );
        this.dictionary = Objects.requireNonNull(
                dictionary, "Dictionary must not be null"
        );
    }

    public GameStepResult handle(final String userInput) {
        Objects.requireNonNull(
                userInput, "User input must not be null"
        );

        if (userInput.isEmpty()) {
            return game.getHint();
        }

        String normalizedWord = dictionary.normalizeWord(userInput);

        validateWord(normalizedWord);

        return game.evaluate(normalizedWord);
    }

    private void validateWord(final String word) {
        if (dictionary.hasValidLength(word)) {
            throw new IllegalArgumentException(
                    "Word has invalid length: " + word +
                            ". Expected length: " + WordleDictionary.WORD_LENGTH
            );
        }

        if (dictionary.hasValidCharacters(word)) {
            throw new IllegalArgumentException(
                    "Word contains invalid characters: " + word +
                            ". Valid pattern: " +
                            WordleDictionary.WORD_PATTERN.pattern()
            );
        }

        if (!dictionary.contains(word)) {
            throw new IllegalArgumentException(
                    "Dictionary does not contain word: " + word
            );
        }
    }
}
