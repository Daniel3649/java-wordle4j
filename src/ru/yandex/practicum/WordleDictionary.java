package ru.yandex.practicum;

import ru.yandex.practicum.logger.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public final class WordleDictionary {

    private static final Pattern WORD_PATTERN =
            Pattern.compile("[а-я]+");

    private final Logger logger;
    private final List<String> words;

    public WordleDictionary(
            final List<String> words,
            final Logger logger
    ) {
        Objects.requireNonNull(
                words,
                "Words must not be null"
        );

        this.logger = Objects.requireNonNull(
                logger,
                "Logger must not be null"
        );


        this.words = prepareWords(words);
    }

    private List<String> prepareWords(final List<String> rawWords) {
        Set<String> uniqueWords = new LinkedHashSet<>();

        for (String rawWord : rawWords) {

            if (rawWord == null) {
                logger.atWarn()
                        .log("Word list contains a null word");
                continue;
            }

            String word = normalizeWord(rawWord);

            if (word.isEmpty()) {
                logger.atWarn()
                        .log("Word list contains an empty word");
                continue;
            }

            if (word.isBlank()) {
                logger.atWarn()
                        .addKeyValue("word_length", word.length())
                        .log("Word list contains a blank word");
                continue;
            }

            if (!hasValidCharacters(word)) {
                logger.atWarn()
                        .addKeyValue("invalid_word", word)
                        .addKeyValue(
                                "expected_pattern",
                                WORD_PATTERN.pattern()
                        )
                        .log(
                                "Word list contains an invalid word: %s. Valid pattern: %s",
                                word,
                                WORD_PATTERN.pattern()
                        );
                continue;
            }

            if (!uniqueWords.add(word)) {
                logger.atWarn()
                        .addKeyValue("repeated_word", word)
                        .log("Word list contains a duplicate word: %s", word);
            }
        }

        if (uniqueWords.isEmpty()) {
            throw new IllegalArgumentException(
                    "Word list must contain at least one valid word"
            );
        }

        return List.copyOf(uniqueWords);
    }

    public String normalizeWord(final String word) {
        Objects.requireNonNull(
                word,
                "Word must not be null"
        );

        return word.toLowerCase(Locale.ROOT)
                .replace('ё', 'е');
    }

    public boolean hasValidCharacters(final String word) {
        Objects.requireNonNull(
                word,
                "Word must not be null"
        );

        return WORD_PATTERN.matcher(
                normalizeWord(word)
        ).matches();
    }

    public List<String> getWords() {
        return words;
    }
}
