package ru.yandex.practicum.logger;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Locale;
import java.util.Collections;
import java.util.regex.Pattern;

public final class LogEvent {

    private static final Pattern KEY_PATTERN =
            Pattern.compile("[a-zA-Z_][a-zA-Z0-9_.-]*");

    private static final Set<String> RESERVED_KEYS = Set.of(
            LogField.TIME.getText(),
            LogField.LEVEL.getText(),
            LogField.MESSAGE.getText()
    );

    private final Logger logger;
    private final LogLevel level;
    private final Map<String, Object> attributes;

    private boolean finalized;
    private String message;

    LogEvent(final Logger logger, final LogLevel level) {
        this.logger = Objects.requireNonNull(
                logger,
                "Логгер не задан."
        );

        this.level = Objects.requireNonNull(
                level,
                "Уровень логирования не задан."
        );

        this.attributes = new LinkedHashMap<>();
    }

    public LogEvent addKeyValue(
            final String key,
            final Object value
    ) {
        ensureNotFinalized();
        validateKey(key);

        attributes.put(key, value);

        return this;
    }

    public void log(final String message) {
        ensureNotFinalized();

        this.message = Objects.requireNonNull(
                message,
                "Сообщение лога не задано."
        );

        this.finalized = true;

        logger.write(this);
    }

    public void log(
            final String format,
            final Object... args
    ) {
        ensureNotFinalized();

        Objects.requireNonNull(format, "Формат сообщения не задан.");
        Objects.requireNonNull(args, "Аргументы сообщения не заданы.");

        String message = String.format(
                Locale.ROOT,
                format,
                args
        );

        log(message);
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        if (!finalized) {
            throw new IllegalStateException(
                    "Событие лога ещё не сформировано."
            );
        }

        return message;
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    private void ensureNotFinalized() {
        if (finalized) {
            throw new IllegalStateException(
                    "Событие лога уже отправлено."
            );
        }
    }

    private static void validateKey(final String key) {
        Objects.requireNonNull(
                key,
                "Ключ поля лога не задан."
        );

        if (!KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException(
                    "Недопустимый ключ поля лога: " + key
            );
        }

        if (RESERVED_KEYS.contains(key)) {
            throw new IllegalArgumentException(
                    "Зарезервированный ключ поля лога: " + key
            );
        }
    }
}
