package ru.yandex.practicum.game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.exception.DictionaryLoadException;
import ru.yandex.practicum.exception.EmptyDictionaryException;
import ru.yandex.practicum.game.dictionary.WordleDictionary;
import ru.yandex.practicum.game.dictionary.WordleDictionaryLoader;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WordleDictionaryLoaderTest {

    @TempDir
    Path dir;

    private PrintWriter log;
    private WordleDictionaryLoader loader;

    @BeforeEach
    void setUp() {
        log = new PrintWriter(System.out, true);
        loader = new WordleDictionaryLoader(log);
    }

    @AfterEach
    void tearDown() {
        log.flush();
    }

    @Test
    void successfullyDictionaryLoadingWithDifferentWordLength() throws Exception {
        Path dictionaryFile = dir.resolve("words_ru.txt");
        List<String> words = List.of(
                "кошка",
                "мышка",
                "чашка",
                "книга",
                "стена",
                "дом",
                "река",
                "солнце",
                "машина",
                "программа"
        );
        Files.write(dictionaryFile, words, StandardCharsets.UTF_8);

        WordleDictionary dictionary = loader.load(dictionaryFile);

        assertIterableEquals(
                List.of("кошка", "мышка", "чашка", "книга", "стена"),
                dictionary.getWordList()
        );
    }

    @Test
    void readsUtf8AndNormalizesAllLinesBeforeReturningDictionary() throws Exception {
        Path file = dir.resolve("words.txt");
        Files.writeString(file,
                "дом\n ШОФЁР \nшофер\n\nabcde\nгерой\nмашина\nгонец",
                StandardCharsets.UTF_8);

        assertIterableEquals(
                List.of("шофер", "герой", "гонец"),
                loader.load(file).getWordList()
        );
    }

    @Test
    void reportsMissingFileAsSystemErrorWithOriginalCause() {
        Path missing = dir.resolve("missing.txt");

        DictionaryLoadException error = assertThrows(
                DictionaryLoadException.class, () -> loader.load(missing));

        assertAll(
                () -> assertTrue(error.getMessage().contains(missing.toString())),
                () -> assertTrue(error.getCause() instanceof IOException)
        );
    }

    @Test
    void rejectsDirectoryInsteadOfDictionaryFile() {
        assertThrows(DictionaryLoadException.class, () -> loader.load(dir));
    }

    @Test
    void rejectsEmptyFile() throws IOException {
        Path file = Files.createFile(dir.resolve("empty.txt"));

        assertThrows(EmptyDictionaryException.class, () -> loader.load(file));
    }

    @Test
    void rejectsFileWithoutSuitableWords() throws IOException {
        Path file = dir.resolve("invalid.txt");
        Files.writeString(file, "дом\nмашина\nhello\n12345\n\n", StandardCharsets.UTF_8);

        assertThrows(EmptyDictionaryException.class, () -> loader.load(file));
    }

    @Test
    void preservesCauseOfMalformedUtf8() throws IOException {
        Path file = dir.resolve("malformed.txt");
        Files.write(file, new byte[] {(byte) 0xc3, (byte) 0x28});

        DictionaryLoadException error = assertThrows(
                DictionaryLoadException.class, () -> loader.load(file));

        assertTrue(error.getCause() instanceof IOException);
    }

    @Test
    void releasesFileAndKeepsSharedLogUsable() throws Exception {
        Path file = dir.resolve("words.txt");
        Files.writeString(file, "герой", StandardCharsets.UTF_8);
        loader.load(file);

        Files.delete(file);
        log.println("Log remains usable after dictionary loading");
        assertAll(
                () -> assertFalse(Files.exists(file)),
                () -> assertFalse(log.checkError())
        );
    }

    @Test
    void rejectsNullPathAndLog() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> loader.load(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> new WordleDictionaryLoader(null))
        );
    }
}
