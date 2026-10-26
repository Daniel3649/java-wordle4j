package ru.yandex.practicum.game.dictionary;

import ru.yandex.practicum.exception.EmptyDictionaryException;
import ru.yandex.practicum.logger.Logger;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public final class WordleDictionary {

    public static final int WORD_LENGTH = 5;

    public static final Pattern WORD_PATTERN =
            Pattern.compile("[а-я]+");

    private final Logger logger;
    private final Set<String> words;

    public WordleDictionary(
            final List<String> words,
            final PrintWriter log
    ) throws EmptyDictionaryException {
        Objects.requireNonNull(
                words,
                "Words must not be null"
        );

        this.logger = new Logger(log);

        this.words = prepareWords(words);
    }

    private Set<String> prepareWords(final List<String> rawWords)
            throws EmptyDictionaryException {
        Set<String> preparedWords = new LinkedHashSet<>();

        for (String rawWord : rawWords) {
            if (rawWord == null) {
                logger.atWarn()
                        .log("Word list contains a null word");
                continue;
            }

            String word = normalizeWord(rawWord);

            if (word.isEmpty()) {
                logger.atWarn()
                        .log("Word list contains an empty or blank word");
                continue;
            }

            if (!hasValidLength(word)) {
                logger.atDebug()
                        .addKeyValue("word", word)
                        .addKeyValue("word_length", word.length())
                        .addKeyValue("required_word_length", WORD_LENGTH)
                        .log(
                                "Word list contains a word with unsuitable length"
                        );
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
                                "Word list contains a word with invalid characters"
                        );
                continue;
            }

            if (!preparedWords.add(word)) {
                logger.atWarn()
                        .addKeyValue("repeated_word", word)
                        .log("Word list contains a duplicate word");
            }
        }

        if (preparedWords.isEmpty()) {
            throw new EmptyDictionaryException();
        }

        return Collections.unmodifiableSet(preparedWords);
    }

    public String normalizeWord(final String word) {
        Objects.requireNonNull(
                word,
                "Word must not be null"
        );

        return word.strip()
                .toLowerCase(Locale.ROOT)
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

    public boolean hasValidLength(final String word) {
        Objects.requireNonNull(
                word,
                "Word must not be null"
        );

        return word.length() == WORD_LENGTH;
    }

    public List<String> getWordList() {
        return List.copyOf(words);
    }

    public boolean contains(final String word) {
        return words.contains(word);
    }
}
