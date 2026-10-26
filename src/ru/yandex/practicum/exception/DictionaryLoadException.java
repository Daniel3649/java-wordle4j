package ru.yandex.practicum.exception;

import java.nio.file.Path;

public final class DictionaryLoadException extends WordleSystemException {

    public DictionaryLoadException(final Path path, final Throwable cause) {
        super("Failed to load dictionary: " + path, cause);
    }
}
