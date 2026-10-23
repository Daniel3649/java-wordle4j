package ru.yandex.practicum.exception.tech;

import java.io.Serial;

public class TechException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2625123658474062554L;

    public TechException(String message) {
        super(message);
    }
}
