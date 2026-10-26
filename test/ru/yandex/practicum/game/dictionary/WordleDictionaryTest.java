package ru.yandex.practicum.game.dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.exception.EmptyDictionaryException;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordleDictionaryTest {
    private PrintWriter log;
    private WordleDictionary dictionary;

    @BeforeEach
    void setUp() throws EmptyDictionaryException {
        log = new PrintWriter(System.out, true);
        dictionary = new WordleDictionary(List.of("герой", "гонец", "шофёр"), log);
    }

    @ParameterizedTest
    @CsvSource({
            "ГЕРОЙ, герой",
            "ШОФЁР, шофер",
            "шофёр, шофер",
            "'  ГеРоЙ  ', герой",
            "'\tШОФЁР\t', шофер"
    })
    void normalizesCaseYoAndSurroundingWhitespace(String input, String expected) {
        assertEquals(expected, dictionary.normalizeWord(input));
    }

    @Test
    void normalizesBlankInputToEmptyString() {
        assertEquals("", dictionary.normalizeWord(" \t\n "));
    }

    @Test
    void filtersInvalidEntriesAndRemovesDuplicatesAfterNormalization() throws Exception {
        WordleDictionary prepared = new WordleDictionary(Arrays.asList(
                null, "", "   ", "дом", "машина", "hello", "12345", "ге-ой",
                " ГЕРОЙ ", "герой", "ШОФЁР", "шофер", "гонец"
        ), log);

        assertIterableEquals(List.of("герой", "шофер", "гонец"), prepared.getWordList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"герой", "шофёр", "ШОФЁР", "съезд", "осень"})
    void acceptsRussianLetters(String word) {
        assertTrue(dictionary.hasValidCharacters(word));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "12345", "гер0й", "ге-ой", "ге ой", "геро!", ""})
    void rejectsNonRussianCharacters(String word) {
        assertFalse(dictionary.hasValidCharacters(word));
    }

    @ParameterizedTest
    @CsvSource({"герой, true", "шофёр, true", "дом, false", "машина, false", "'', false"})
    void validatesWordLength(String word, boolean expected) {
        assertEquals(expected, dictionary.hasValidLength(word));
    }

    @Test
    void findsOnlyWordsPresentInPreparedDictionary() {
        assertAll(
                () -> assertTrue(dictionary.contains("герой")),
                () -> assertTrue(dictionary.contains("шофер")),
                () -> assertFalse(dictionary.contains("касса"))
        );
    }

    @Test
    void doesNotRetainMutableSourceListOrExposeMutableWords() throws Exception {
        var source = new java.util.ArrayList<>(List.of("герой"));
        WordleDictionary prepared = new WordleDictionary(source, log);
        source.add("гонец");

        assertAll(
                () -> assertEquals(List.of("герой"), prepared.getWordList()),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> prepared.getWordList().add("гонец"))
        );
    }

    @Test
    void rejectsEmptyDictionaryAndDictionaryWithoutValidWords() {
        assertAll(
                () -> assertThrows(EmptyDictionaryException.class,
                        () -> new WordleDictionary(List.of(), log)),
                () -> assertThrows(EmptyDictionaryException.class,
                        () -> new WordleDictionary(List.of("дом", "hello", ""), log))
        );
    }

    @Test
    void rejectsNullArguments() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new WordleDictionary(null, log)),
                () -> assertThrows(NullPointerException.class,
                        () -> new WordleDictionary(List.of("герой"), null)),
                () -> assertThrows(NullPointerException.class,
                        () -> dictionary.normalizeWord(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> dictionary.hasValidLength(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> dictionary.hasValidCharacters(null))
        );
    }
}
