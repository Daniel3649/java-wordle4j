package ru.yandex.practicum.game.dictionary;

import ru.yandex.practicum.logger.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public final class WordleDictionaryLoader {

    private final Logger logger;

    public WordleDictionaryLoader(final Logger logger) {
        this.logger = Objects.requireNonNull(
                logger,
                "Logger must not be null"
        );
    }

    public WordleDictionary load(final Path path)
            throws IOException {

        Objects.requireNonNull(
                path,
                "Dictionary path must not be null"
        );

        if (!Files.isRegularFile(path)) {
            throw new IOException(
                    "Dictionary path must point to a regular file: " + path
            );
        }

        List<String> words = Files.readAllLines(
                path,
                StandardCharsets.UTF_8
        );

        logger.atInfo()
                .addKeyValue("path", path)
                .log("Words are loaded from file: %s", path.toString());

        return new WordleDictionary(words, logger);
    }
}
