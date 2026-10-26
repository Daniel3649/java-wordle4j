package ru.yandex.practicum.game.engine;

import ru.yandex.practicum.exception.EmptyDictionaryException;
import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.exception.InvalidWordCharactersException;
import ru.yandex.practicum.exception.InvalidWordLengthException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;
import ru.yandex.practicum.game.dictionary.WordleDictionary;
import ru.yandex.practicum.game.step.GameStepResult;
import ru.yandex.practicum.logger.Logger;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

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

    public WordleGame(final WordleDictionary dictionary, final PrintWriter log)
            throws EmptyDictionaryException {
        this(dictionary, log, ThreadLocalRandom.current());
    }

    WordleGame(final WordleDictionary dictionary, final PrintWriter log,
               final RandomGenerator random) throws EmptyDictionaryException {
        this.dictionary = Objects.requireNonNull(
                dictionary, "Dictionary can't be null"
        );

        List<String> allDictionaryWords = List.copyOf(dictionary.getWordList());

        if (allDictionaryWords.isEmpty()) {
            throw new EmptyDictionaryException();
        }

        this.logger = new Logger(log);

        int randomIndex = Objects.requireNonNull(random, "Random generator must not be null")
                .nextInt(allDictionaryWords.size());

        this.secret = allDictionaryWords.get(randomIndex);
        this.remainingSteps = STEP_COUNT;
        this.wordCandidates = new ArrayList<>(allDictionaryWords);

        logger.atInfo()
                .addKeyValue("secret", secret)
                .addKeyValue("remaining_steps", remainingSteps)
                .addKeyValue("candidate_count", wordCandidates.size())
                .log("Game started");
    }

    public GameStepResult evaluate(final String guess)
            throws InvalidWordLengthException,
            InvalidWordCharactersException,
            WordNotFoundInDictionaryException {
        if (isFinished()) {
            throw new IllegalStateException(
                    "Game is already finished"
            );
        }

        Objects.requireNonNull(
                guess, "Guess word can't be null"
        );

        String normalizedGuess = dictionary.normalizeWord(guess);

        validateWord(normalizedGuess);

        LetterState[] state = compareWords(normalizedGuess, secret);

        logger.atInfo()
                .addKeyValue("guess_word", normalizedGuess)
                .addKeyValue("letter_states", Arrays.toString(state))
                .log("Guess word %s was compared with secret. Result: %s",
                        normalizedGuess, Arrays.toString(state));

        filterCandidates(normalizedGuess, state);

        remainingSteps--;
        logger.atInfo()
                .addKeyValue("remaining_steps", remainingSteps)
                .addKeyValue("candidate_count", wordCandidates.size())
                .log("Remaining steps: %s", remainingSteps);

        if (normalizedGuess.equals(secret)) {
            won = true;

            logger.atInfo()
                    .addKeyValue("remaining_steps", remainingSteps)
                    .log("Game won");
        } else if (remainingSteps == 0) {
            logger.atInfo()
                    .addKeyValue("secret", secret)
                    .log("Game lost");
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

        try {
            GameStepResult result = evaluate(chosenWord);
            return new GameStepResult.Hint(result.state(), chosenWord);
        } catch (GameException e) {
            throw new IllegalStateException(
                    "Hint selected from dictionary failed validation: " + chosenWord,
                    e
            );
        }
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

    private void validateWord(final String word)
            throws InvalidWordLengthException,
            InvalidWordCharactersException,
            WordNotFoundInDictionaryException {
        if (!dictionary.hasValidLength(word)) {
            throw new InvalidWordLengthException(
                    WordleDictionary.WORD_LENGTH
            );
        }

        if (!dictionary.hasValidCharacters(word)) {
            throw new InvalidWordCharactersException();
        }

        if (!dictionary.contains(word)) {
            throw new WordNotFoundInDictionaryException(word);
        }
    }
}
