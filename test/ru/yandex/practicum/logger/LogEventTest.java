package ru.yandex.practicum.logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogEventTest {
    private PrintStream originalOut;
    private PrintStream capturedOut;
    private ByteArrayOutputStream output;
    private PrintWriter log;
    private LogEvent event;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        output = new ByteArrayOutputStream();
        capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOut);
        log = new PrintWriter(System.out, true);
        event = new Logger(log).atInfo();
    }

    @AfterEach
    void tearDown() {
        log.flush();
        System.setOut(originalOut);
        capturedOut.close();
    }

    @Test
    void storesAttributesAndReturnsSameEventForChaining() {
        assertSame(event, event.addKeyValue("word", "герой"));
        event.addKeyValue("remaining_steps", 6);

        assertEquals(Map.of("word", "герой", "remaining_steps", 6), event.getAttributes());
        assertEquals(LogLevel.INFO, event.getLevel());
        assertThrows(UnsupportedOperationException.class,
                () -> event.getAttributes().put("other", 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a b", "1word", "word=value", "_time", "level", "_msg"})
    void rejectsInvalidAndReservedAttributeKeys(String key) {
        assertThrows(IllegalArgumentException.class, () -> event.addKeyValue(key, "value"));
    }

    @Test
    void rejectsNullKeysMessagesAndFormats() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> event.addKeyValue(null, "value")),
                () -> assertThrows(NullPointerException.class,
                        () -> event.log((String) null)),
                () -> assertThrows(NullPointerException.class,
                        () -> event.log(null, 1)),
                () -> assertThrows(NullPointerException.class,
                        () -> event.log("%s", (Object[]) null))
        );
    }

    @Test
    void allowsNullAttributeValues() {
        event.addKeyValue("value", null).log("Необязательное значение.");

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("value=\"null\""));
    }

    @Test
    void formatsMessagesWithArguments() {
        event.log("Слово %s, попыток %d, оценка %.1f", "герой", 5, 2.5);

        assertEquals("Слово герой, попыток 5, оценка 2.5", event.getMessage());
        assertTrue(output.toString(StandardCharsets.UTF_8)
                .contains("_msg=\"Слово герой, попыток 5, оценка 2.5\""));
    }

    @Test
    void rejectsReadingMessageBeforeItIsSet() {
        assertThrows(IllegalStateException.class, event::getMessage);
    }

    @Test
    void rejectsChangesAndRepeatedWritesAfterLogging() {
        event.log("Первое сообщение.");

        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> event.log("Второе сообщение.")),
                () -> assertThrows(IllegalStateException.class, () -> event.log("%s", "Второе сообщение.")),
                () -> assertThrows(IllegalStateException.class, () -> event.addKeyValue("word", "герой")),
                () -> assertEquals(1, output.toString(StandardCharsets.UTF_8).lines().count()),
                () -> assertEquals("Первое сообщение.", event.getMessage())
        );
    }
}
