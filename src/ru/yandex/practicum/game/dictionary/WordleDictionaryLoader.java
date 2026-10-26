package ru.yandex.practicum.game.dictionary;

import ru.yandex.practicum.exception.DictionaryLoadException;
import ru.yandex.practicum.exception.EmptyDictionaryException;
import ru.yandex.practicum.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public final class WordleDictionaryLoader {

    private final Logger logger;
    private final PrintWriter log;

    public WordleDictionaryLoader(final PrintWriter log) {
        this.log = Objects.requireNonNull(
                log,
                "Поток записи лога не задан."
        );
        this.logger = new Logger(log);
    }

    public WordleDictionary load(final Path path)
            throws DictionaryLoadException, EmptyDictionaryException {

        Objects.requireNonNull(
                path,
                "Путь к файлу словаря не задан."
        );

        List<String> words = new ArrayList<>();
        try {
            if (!Files.isRegularFile(path)) {
                throw new IOException(
                        "Путь к словарю должен указывать на обычный файл: " + path
                );
            }

            try (BufferedReader reader = Files.newBufferedReader(
                    path,
                    StandardCharsets.UTF_8
            )) {
                String word;
                while ((word = reader.readLine()) != null) {
                    words.add(word);
                }
            }
        } catch (IOException | SecurityException e) {
            throw new DictionaryLoadException(path, e);
        }

        logger.atInfo()
                .addKeyValue("path", path)
                .addKeyValue("word_count", words.size())
                .log("Слова загружены из файла: %s", path.toString());

        return new WordleDictionary(words, log);
    }
}
