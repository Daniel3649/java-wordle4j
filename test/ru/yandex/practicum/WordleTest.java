package ru.yandex.practicum;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.game.dictionary.WordleDictionary;
import ru.yandex.practicum.game.engine.WordleGame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordleTest {
    private PrintStream originalOut;
    private PrintStream originalErr;
    private PrintStream capturedOut;
    private PrintStream capturedErr;
    private ByteArrayOutputStream output;
    private ByteArrayOutputStream errors;
    private PrintWriter log;
    private WordleGame game;

    @BeforeEach
    void setUp() throws Exception {
        originalOut = System.out;
        originalErr = System.err;
        log = new PrintWriter(System.out, true);
        output = new ByteArrayOutputStream();
        errors = new ByteArrayOutputStream();
        capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8);
        capturedErr = new PrintStream(errors, true, StandardCharsets.UTF_8);
        System.setOut(capturedOut);
        System.setErr(capturedErr);
        game = new WordleGame(new WordleDictionary(List.of("герой", "гонец"), log), log);
    }

    @AfterEach
    void tearDown() {
        log.flush();
        System.setOut(originalOut);
        System.setErr(originalErr);
        capturedOut.close();
        capturedErr.close();
    }

    @Test
    void promptsBeforeReadingInput() throws Exception {
        Reader input = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) {
                assertTrue(output.toString(StandardCharsets.UTF_8).contains("Enter"),
                        "Player must see a prompt before input is requested");
                return -1;
            }

            @Override
            public void close() {
            }
        };

        try (Scanner scanner = new Scanner(input)) {
            Wordle.gameLoop(game, scanner);
        }
        assertEquals(6, game.getRemainingSteps());
    }

    @Test
    void displaysFriendlyValidationErrorsAndAllowsRecovery() throws Exception {
        play("кот\nhello\nстена\n   \n  " + game.getSecret().toUpperCase(java.util.Locale.ROOT) + "  \n");

        String text = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(text.contains("Введите слово из 5 букв.")),
                () -> assertTrue(text.contains("только русские буквы")),
                () -> assertTrue(text.contains("нет в словаре")),
                () -> assertTrue(text.contains("+++++")),
                () -> assertTrue(game.isWon()),
                () -> assertEquals(5, game.getRemainingSteps()),
                () -> assertFalse(text.contains("Exception")),
                () -> assertFalse(text.contains("Game started")),
                () -> assertEquals("", errors.toString(StandardCharsets.UTF_8))
        );
    }

    @Test
    void emptyLinePlaysHintAndStopsReadingAfterVictory() throws Exception {
        game = new WordleGame(new WordleDictionary(List.of("герой"), log), log);
        try (Scanner scanner = new Scanner("\nunused input\n")) {
            Wordle.gameLoop(game, scanner);

            assertAll(
                    () -> assertTrue(game.isWon()),
                    () -> assertEquals(5, game.getRemainingSteps()),
                    () -> assertEquals("unused input", scanner.nextLine()),
                    () -> assertTrue(output.toString(StandardCharsets.UTF_8).contains(
                            "герой" + System.lineSeparator() + "+++++"))
            );
        }
    }

    @Test
    void stopsReadingAfterSixValidWrongGuesses() throws Exception {
        String wrong = game.getSecret().equals("герой") ? "гонец" : "герой";
        try (Scanner scanner = new Scanner((wrong + "\n").repeat(6) + "unused input\n")) {
            Wordle.gameLoop(game, scanner);

            assertAll(
                    () -> assertTrue(game.isFinished()),
                    () -> assertFalse(game.isWon()),
                    () -> assertEquals(0, game.getRemainingSteps()),
                    () -> assertEquals("unused input", scanner.nextLine()),
                    () -> assertTrue(output.toString(StandardCharsets.UTF_8).contains("Remaining steps: 0"))
            );
        }
    }

    @Test
    void completesHintOnlyGameWithinSixTurns() throws Exception {
        List<String> words = List.of("лайка", "майка", "пайка", "байка", "гайка", "чайка");
        game = new WordleGame(new WordleDictionary(words, log), log);
        play("\n".repeat(6));

        assertTrue(game.isWon());
        assertTrue(game.getRemainingSteps() >= 0);
        assertTrue(game.getRemainingSteps() < 6);
    }

    @Test
    void emptyInputStreamExitsWithoutConsumingAttemptOrLooping() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> play(""));

        assertEquals(6, game.getRemainingSteps());
        assertFalse(game.isFinished());
    }

    @Test
    void doesNotReadOrPrintPromptForAlreadyFinishedGame() throws Exception {
        game.evaluate(game.getSecret());

        play("гонец\n");

        assertEquals("", output.toString(StandardCharsets.UTF_8));
        assertEquals(5, game.getRemainingSteps());
    }

    @Test
    void propagatesInputFailureForSystemLogging() {
        IOException cause = new IOException("Console input failed");
        Reader broken = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw cause;
            }

            @Override
            public void close() {
            }
        };

        try (Scanner scanner = new Scanner(broken)) {
            IOException error = assertThrows(IOException.class, () -> Wordle.gameLoop(game, scanner));
            assertSame(cause, error.getCause());
            assertFalse(output.toString(StandardCharsets.UTF_8).contains("Console input failed"));
            assertEquals("", errors.toString(StandardCharsets.UTF_8));
        }
    }

    private void play(String input) throws IOException {
        try (Scanner scanner = new Scanner(input)) {
            Wordle.gameLoop(game, scanner);
        }
    }

}
