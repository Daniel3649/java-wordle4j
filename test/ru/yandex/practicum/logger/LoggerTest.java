package ru.yandex.practicum.logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.yandex.practicum.game.dictionary.WordleDictionary;
import ru.yandex.practicum.game.engine.WordleGame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggerTest {
    private PrintStream originalOut;
    private PrintStream capturedOut;
    private ByteArrayOutputStream output;
    private PrintWriter log;
    private Logger logger;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        output = new ByteArrayOutputStream();
        capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOut);
        log = new PrintWriter(System.out, true);
        logger = new Logger(log);
    }

    @AfterEach
    void tearDown() {
        log.flush();
        System.setOut(originalOut);
        capturedOut.close();
    }

    @ParameterizedTest
    @EnumSource(LogLevel.class)
    void writesTimestampLevelMessageAndAttributes(LogLevel level) {
        Logger debugLogger = new Logger(log, true);
        LogEvent event = switch (level) {
            case INFO -> debugLogger.atInfo();
            case WARN -> debugLogger.atWarn();
            case ERROR -> debugLogger.atError();
            case DEBUG -> debugLogger.atDebug();
        };
        event.addKeyValue("remaining_steps", 6).log("Game started");

        String text = output.toString(StandardCharsets.UTF_8);
        Matcher timestamp = Pattern.compile("_time=\"([^\"]+)\"").matcher(text);
        assertTrue(timestamp.find());
        assertDoesNotThrow(() -> Instant.parse(timestamp.group(1)));
        assertAll(
                () -> assertTrue(text.contains("level=\"" + level.getText() + "\"")),
                () -> assertTrue(text.contains("_msg=\"Game started\"")),
                () -> assertTrue(text.contains("remaining_steps=\"6\"")),
                () -> assertEquals(1, text.lines().count())
        );
    }

    @Test
    void skipsDebugMessagesByDefault() {
        logger.atDebug().log("Hidden debug message");

        assertEquals("", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void escapesQuotesBackslashesAndControlCharactersIntoSingleLine() {
        logger.atError()
                .addKeyValue("path", "C:\\words\".txt")
                .log("quote=\"; slash=\\; newline=\n; return=\r; tab=\t");

        String text = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertEquals(1, text.lines().count()),
                () -> assertTrue(text.contains(
                        "_msg=\"quote=\\\"; slash=\\\\; newline=\\n; return=\\r; tab=\\t\"")),
                () -> assertTrue(text.contains("path=\"C:\\\\words\\\".txt\""))
        );
    }

    @Test
    void reportsUnderlyingWriterFailureInsteadOfSilentlyLosingLogs() {
        Writer broken = new Writer() {
            @Override
            public void write(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("Log storage is unavailable");
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        try (PrintWriter failedLog = new PrintWriter(broken)) {
            assertThrows(IllegalStateException.class,
                    () -> new Logger(failedLog).atError().log("System error"));
        }
    }

    @Test
    void rejectsClosedWriter() {
        PrintWriter closed = new PrintWriter(new StringWriter());
        closed.close();

        assertThrows(IllegalStateException.class,
                () -> new Logger(closed).atInfo().log("Cannot write"));
    }

    @Test
    void logsGameStateActualLetterStatesAndVictory() throws Exception {
        WordleDictionary dictionary = new WordleDictionary(List.of("герой"), log);
        WordleGame game = new WordleGame(dictionary, log);
        game.evaluate("ГЕРОЙ");

        String text = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(text.contains("secret=\"герой\"")),
                () -> assertTrue(text.contains("remaining_steps=\"6\"")),
                () -> assertTrue(text.contains("remaining_steps=\"5\"")),
                () -> assertTrue(text.contains("candidate_count=\"1\"")),
                () -> assertTrue(text.contains("guess_word=\"герой\"")),
                () -> assertTrue(text.contains(
                        "letter_states=\"[CORRECT, CORRECT, CORRECT, CORRECT, CORRECT]\"")),
                () -> assertTrue(text.contains("Result: [CORRECT, CORRECT, CORRECT, CORRECT, CORRECT]")),
                () -> assertTrue(text.contains("Game won")),
                () -> assertFalse(text.contains("Result: герой")),
                () -> assertFalse(log.checkError())
        );
    }

    @Test
    void logsDefeatAndExhaustedAttempts() throws Exception {
        WordleGame game = new WordleGame(
                new WordleDictionary(List.of("герой", "гонец"), log), log);
        String wrong = game.getSecret().equals("герой") ? "гонец" : "герой";
        for (int turn = 0; turn < 6; turn++) {
            game.evaluate(wrong);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("remaining_steps=\"0\""));
        assertTrue(text.contains("Game lost"));
    }

    @Test
    void rejectsMissingWriter() {
        assertThrows(NullPointerException.class, () -> new Logger(null));
    }
}
