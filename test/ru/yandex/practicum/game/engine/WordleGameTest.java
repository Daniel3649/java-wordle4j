package ru.yandex.practicum.game.engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.exception.InvalidWordCharactersException;
import ru.yandex.practicum.exception.InvalidWordLengthException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;
import ru.yandex.practicum.game.dictionary.WordleDictionary;
import ru.yandex.practicum.game.step.GameStepResult;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordleGameTest {
    private static List<String> similarWords;
    private PrintWriter log;
    private WordleGame game;

    @BeforeAll
    static void prepareWords() {
        similarWords = List.of("лайка", "майка", "пайка", "байка", "гайка", "чайка");
    }

    @BeforeEach
    void setUp() throws Exception {
        log = new PrintWriter(System.out, true);
        game = gameWithSecret("герой", List.of("герой", "гонец", "касса", "маска"));
    }

    @Test
    void startsWithSixAttemptsAndSecretFromDictionary() throws Exception {
        WordleDictionary dictionary = new WordleDictionary(similarWords, log);
        WordleGame randomlyInitialized = new WordleGame(dictionary, log);

        assertAll(
                () -> assertEquals(6, randomlyInitialized.getRemainingSteps()),
                () -> assertFalse(randomlyInitialized.isWon()),
                () -> assertFalse(randomlyInitialized.isFinished()),
                () -> assertTrue(dictionary.contains(randomlyInitialized.getSecret()))
        );
    }

    @ParameterizedTest
    @CsvSource({
            "герой, гонец, +^-^-",
            "касса, маска, -++^+",
            "колос, сокол, ^+^+^",
            "топот, потоп, ^+^+-",
            "шишка, шапка, +--++",
            "банан, кабан, -+^++",
            "кабан, банан, ^+-++",
            "пепел, пилот, +-^--",
            "герой, маска, -----",
            "осина, сосна, ^^-++",
            "сосна, осина, ^^-++",
            "стена, стена, +++++"
    })
    void comparesLettersIncludingRepeatedLetters(String secret, String guess, String feedback)
            throws Exception {
        WordleGame checkedGame = gameWithSecret(secret, List.of(secret, guess));

        GameStepResult result = checkedGame.evaluate(guess);

        assertAll(
                () -> assertTrue(result instanceof GameStepResult.Guess),
                () -> assertArrayEquals(states(feedback), result.state()),
                () -> assertEquals(5, checkedGame.getRemainingSteps()),
                () -> assertEquals(secret.equals(guess), checkedGame.isWon())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"герой", "ГЕРОЙ", "  ГеРоЙ  ", "\tгерой\t"})
    void normalizesInputBeforeCheckingVictory(String guess) throws Exception {
        game.evaluate(guess);

        assertAll(
                () -> assertTrue(game.isWon()),
                () -> assertTrue(game.isFinished()),
                () -> assertEquals(5, game.getRemainingSteps())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"шофер", "шофёр", "ШОФЁР", "  ШоФёР  "})
    void treatsYoAndEAsSameLetter(String guess) throws Exception {
        WordleGame checkedGame = gameWithSecret("шофер", List.of("шофёр"));

        assertArrayEquals(states("+++++"), checkedGame.evaluate(guess).state());
        assertTrue(checkedGame.isWon());
    }

    static Stream<Arguments> invalidWords() {
        return Stream.of(
                Arguments.of("", InvalidWordLengthException.class),
                Arguments.of("   ", InvalidWordLengthException.class),
                Arguments.of("кот", InvalidWordLengthException.class),
                Arguments.of("машина", InvalidWordLengthException.class),
                Arguments.of("hello", InvalidWordCharactersException.class),
                Arguments.of("12345", InvalidWordCharactersException.class),
                Arguments.of("ге-ой", InvalidWordCharactersException.class),
                Arguments.of("ге ой", InvalidWordCharactersException.class),
                Arguments.of("гер0й", InvalidWordCharactersException.class),
                Arguments.of("стена", WordNotFoundInDictionaryException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidWords")
    void invalidInputDoesNotConsumeAttemptsOrChangeCandidates(
            String word, Class<? extends GameException> exceptionType) throws Exception {
        game = gameWithSecret("герой", List.of("герой"));
        assertThrows(exceptionType, () -> game.evaluate(word));
        assertAll(
                () -> assertEquals(6, game.getRemainingSteps()),
                () -> assertFalse(game.isFinished()),
                () -> assertFalse(game.isWon())
        );

        // The only solution must remain available to the hint algorithm.
        GameStepResult.Hint hint = (GameStepResult.Hint) game.getHint();
        assertEquals("герой", hint.guess());
        assertTrue(game.isWon());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ГОНЕЦ", "  ГоНеЦ  ", "\tгонец\t"})
    void normalizesWrongGuessBeforeComparingAndFilteringCandidates(String guess) throws Exception {
        assertArrayEquals(states("+^-^-"), game.evaluate(guess).state());
        assertFalse(game.isWon());

        GameStepResult.Hint hint = (GameStepResult.Hint) game.getHint();

        assertEquals("герой", hint.guess());
        assertTrue(game.isWon());
        assertEquals(4, game.getRemainingSteps());
    }

    @Test
    void validWrongGuessConsumesExactlyOneAttempt() throws Exception {
        game.evaluate("гонец");

        assertAll(
                () -> assertEquals(5, game.getRemainingSteps()),
                () -> assertFalse(game.isWon()),
                () -> assertFalse(game.isFinished())
        );
    }

    @Test
    void losesAfterExactlySixValidWrongGuesses() throws Exception {
        for (int turn = 1; turn <= 6; turn++) {
            game.evaluate("гонец");
            assertEquals(6 - turn, game.getRemainingSteps());
            assertEquals(turn == 6, game.isFinished());
        }

        assertFalse(game.isWon());
        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> game.evaluate("герой")),
                () -> assertThrows(IllegalStateException.class, game::getHint),
                () -> assertEquals(0, game.getRemainingSteps())
        );
    }

    @Test
    void canWinOnLastAttempt() throws Exception {
        for (int turn = 0; turn < 5; turn++) {
            game.evaluate("гонец");
        }
        game.evaluate("герой");

        assertAll(
                () -> assertTrue(game.isWon()),
                () -> assertTrue(game.isFinished()),
                () -> assertEquals(0, game.getRemainingSteps())
        );
    }

    @Test
    void rejectsMovesAndHintsAfterVictory() throws Exception {
        game.evaluate("герой");

        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> game.evaluate("гонец")),
                () -> assertThrows(IllegalStateException.class, game::getHint),
                () -> assertEquals(5, game.getRemainingSteps())
        );
    }

    @Test
    void firstHintIsDictionaryWordAndCountsAsTurn() {
        GameStepResult.Hint hint = (GameStepResult.Hint) game.getHint();

        assertAll(
                () -> assertTrue(List.of("герой", "гонец", "касса", "маска").contains(hint.guess())),
                () -> assertEquals(5, game.getRemainingSteps()),
                () -> assertArrayEquals(referenceFeedback(hint.guess(), "герой"), hint.state()),
                () -> assertEquals(hint.guess().equals("герой"), game.isWon())
        );
    }

    @Test
    void hintWinsImmediatelyForSingleWordDictionary() throws Exception {
        WordleGame singleWordGame = gameWithSecret("герой", List.of("герой"));

        GameStepResult.Hint hint = (GameStepResult.Hint) singleWordGame.getHint();

        assertAll(
                () -> assertEquals("герой", hint.guess()),
                () -> assertArrayEquals(states("+++++"), hint.state()),
                () -> assertTrue(singleWordGame.isWon()),
                () -> assertEquals(5, singleWordGame.getRemainingSteps())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"лайка", "майка", "пайка", "байка", "гайка", "чайка"})
    void hintsKeepSolutionAndNeverRepeatPreviousManualGuesses(String secret) throws Exception {
        WordleGame checkedGame = gameWithSecret(secret, similarWords);
        for (String word : similarWords) {
            if (!word.equals(secret)) {
                checkedGame.evaluate(word);
            }
        }

        GameStepResult.Hint hint = (GameStepResult.Hint) checkedGame.getHint();

        assertAll(
                () -> assertEquals(secret, hint.guess()),
                () -> assertTrue(checkedGame.isWon()),
                () -> assertEquals(0, checkedGame.getRemainingSteps())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"сосна", "осина", "касса", "маска", "колос", "сокол"})
    void hintsRespectAllPreviousFeedbackIncludingDuplicateLetters(String secret) throws Exception {
        List<String> words = List.of("сосна", "осина", "касса", "маска", "колос", "сокол");
        WordleGame checkedGame = gameWithSecret(secret, words);
        Map<String, LetterState[]> history = new LinkedHashMap<>();
        String manualGuess = words.stream().filter(word -> !word.equals(secret)).findFirst().orElseThrow();
        history.put(manualGuess, checkedGame.evaluate(manualGuess).state());

        for (int turn = 1; turn < 6 && !checkedGame.isFinished(); turn++) {
            GameStepResult.Hint hint = (GameStepResult.Hint) checkedGame.getHint();
            assertTrue(words.contains(hint.guess()));
            assertFalse(history.containsKey(hint.guess()), "Hint must not repeat any previous guess");
            for (var previous : history.entrySet()) {
                assertArrayEquals(previous.getValue(), referenceFeedback(previous.getKey(), hint.guess()),
                        "Hint must satisfy feedback for " + previous.getKey());
            }
            assertArrayEquals(referenceFeedback(hint.guess(), secret), hint.state());
            history.put(hint.guess(), hint.state());
        }

        assertTrue(checkedGame.isWon(), "Six candidates must be solvable in at most six distinct turns");
        assertEquals(6 - history.size(), checkedGame.getRemainingSteps());
    }

    @Test
    void rejectsNullInputAndDependencies() throws Exception {
        WordleDictionary dictionary = new WordleDictionary(List.of("герой"), log);
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> game.evaluate(null)),
                () -> assertThrows(NullPointerException.class, () -> new WordleGame(null, log)),
                () -> assertThrows(NullPointerException.class, () -> new WordleGame(dictionary, null)),
                () -> assertThrows(NullPointerException.class,
                        () -> new WordleGame(dictionary, log, null)),
                () -> assertEquals(6, game.getRemainingSteps())
        );
    }

    private WordleGame gameWithSecret(String secret, List<String> words) throws Exception {
        Set<String> ordered = new LinkedHashSet<>();
        ordered.add(secret);
        ordered.addAll(words);
        Random firstWord = new Random(0) {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
        return new WordleGame(new WordleDictionary(new ArrayList<>(ordered), log), log, firstWord);
    }

    private static LetterState[] states(String feedback) {
        return feedback.chars().mapToObj(symbol -> switch (symbol) {
            case '+' -> LetterState.CORRECT;
            case '^' -> LetterState.PRESENT;
            case '-' -> LetterState.ABSENT;
            default -> throw new IllegalArgumentException("Unknown feedback symbol");
        }).toArray(LetterState[]::new);
    }

    private static LetterState[] referenceFeedback(String guess, String secret) {
        StringBuilder unused = new StringBuilder(secret);
        LetterState[] result = new LetterState[5];
        Arrays.fill(result, LetterState.ABSENT);
        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == secret.charAt(i)) {
                result[i] = LetterState.CORRECT;
                unused.setCharAt(i, '\0');
            }
        }
        for (int i = 0; i < 5; i++) {
            if (result[i] != LetterState.CORRECT) {
                int index = unused.indexOf(String.valueOf(guess.charAt(i)));
                if (index >= 0) {
                    result[i] = LetterState.PRESENT;
                    unused.setCharAt(index, '\0');
                }
            }
        }
        return result;
    }
}
