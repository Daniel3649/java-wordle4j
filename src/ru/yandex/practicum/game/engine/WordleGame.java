package ru.yandex.practicum.game.engine;

import ru.yandex.practicum.game.dictionary.WordleDictionary;
import ru.yandex.practicum.game.step.GameStepResult;
import ru.yandex.practicum.logger.Logger;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
public final class WordleGame {
    private static final int STEP_COUNT = 6;

    private final String secret;
    private int remainingSteps;
    private final List<String> wordCandidates;
    private final Logger logger;
    private final WordleDictionary dictionary;

    private boolean won;

    public WordleGame(final WordleDictionary dictionary, final Logger logger) {
        this.dictionary = Objects.requireNonNull(
                dictionary, "Dictionary can't be null"
        );

        List<String> allDictionaryWords = List.copyOf(dictionary.getWordList());

        if (allDictionaryWords.isEmpty()) {
            throw new IllegalArgumentException(
                    "Dictionary is empty"
            );
        }

        this.logger = Objects.requireNonNull(
                logger, "Logger can't be null"
        );

        int randomIndex = ThreadLocalRandom.current()
                .nextInt(allDictionaryWords.size());

        this.secret = allDictionaryWords.get(randomIndex);
        logger.atInfo()
                .log("Secret was chosen");

        this.remainingSteps = STEP_COUNT;
        this.wordCandidates = new ArrayList<>(allDictionaryWords);
    }

    public GameStepResult evaluate(final String guess) {
        if (isFinished()) {
            throw new IllegalStateException(
                    "Game is already finished"
            );
        }

        Objects.requireNonNull(
                guess, "Guess word can't be null"
        );

        String normalizeGuess = dictionary.normalizeWord(guess);

        validateWord(normalizeGuess);

        LetterState[] state = compareWords(guess, secret);

        logger.atInfo()
                .addKeyValue("guess_word", guess)
                .log("Guess word %s was compared with secret. Result: %s",
                        guess, secret, Arrays.toString(state));

        filterCandidates(guess, state);

        remainingSteps--;
        logger.atInfo()
                .addKeyValue("remaining_steps", remainingSteps)
                .log("Remaining steps: %s", remainingSteps);

        if (guess.equals(secret)) {
            won = true;

            logger.atInfo()
                    .log("Game won");
        }

        return new GameStepResult.Guess(state);
    }

    private LetterState[] compareWords(
            final String guess,
            final String secret
    ) {
        Objects.requireNonNull(
                guess, "Guess word can't be null"
        );
        Objects.requireNonNull(
                secret, "Secret word can't be null"
        );

        int length = guess.length();

        LetterState[] state = new LetterState[length];
        Arrays.fill(state, LetterState.ABSENT);

        Map<Character, Integer> letterCounts = new HashMap<>();

        for (int i = 0; i < length; i++) {
            letterCounts.merge(
                    secret.charAt(i), 1, Integer::sum
            );
        }

        for (int i = 0; i < length; i++) {
            char secretLetter = secret.charAt(i);
            char guessLetter = guess.charAt(i);

            if (secretLetter == guessLetter) {
                state[i] = LetterState.CORRECT;

                letterCounts.merge(
                        secretLetter, -1, Integer::sum
                );
            }
        }

        for (int i = 0; i < length; i++) {
            if (state[i] == LetterState.CORRECT) {
                continue;
            }

            char letter = guess.charAt(i);
            int count = letterCounts.getOrDefault(letter, 0);

            if (count > 0) {
                state[i] = LetterState.PRESENT;
                letterCounts.put(letter, count - 1);
            }
        }

        return state;
    }

    private void filterCandidates(
            final String guess,
            final LetterState[] actualResult
    ) {
        wordCandidates.removeIf(candidate ->
                !Arrays.equals(
                        actualResult,
                        compareWords(guess, candidate)
                )
        );
    }

    public GameStepResult getHint() {
        if (isFinished()) {
            throw new IllegalStateException(
                    "Game is already finished"
            );
        }

        int randomIndex = ThreadLocalRandom.current()
                .nextInt(wordCandidates.size());
        String chosenWord = wordCandidates.get(randomIndex);

        logger.atInfo()
                .addKeyValue("hint", chosenWord)
                .log("Hint was chosen: %s", chosenWord);

        LetterState[] state = compareWords(chosenWord, secret);
        logger.atInfo()
                .addKeyValue("guess_word", chosenWord)
                .log("Chosen word %s was compared with secret. Result: %s",
                        chosenWord, secret, Arrays.toString(state));

        filterCandidates(chosenWord, state);
        return new GameStepResult.Hint(state, chosenWord);
    }

    public boolean isWon() {
        return won;
    }

    public boolean isFinished() {
        return won || remainingSteps == 0;
    }

    public int getRemainingSteps() {
        return remainingSteps;
    }

    public String getSecret() {
        return secret;
    }

    private void validateWord(final String word) {
        if (!dictionary.hasValidLength(word)) {
            throw new IllegalArgumentException(
                    "Word has invalid length: " + word +
                            ". Expected length: " + WordleDictionary.WORD_LENGTH
            );
        }

        if (!dictionary.hasValidCharacters(word)) {
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
