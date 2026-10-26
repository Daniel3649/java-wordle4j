package ru.yandex.practicum.game.formatter;

import ru.yandex.practicum.game.engine.WordleGame;

import java.util.Objects;

public final class GameResultFormatter {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String VICTORY_LABEL = "Вы победили! Поздравляем!";
    private static final String LOSE_LABEL = "Вы проиграли.";
    private static final String SECRET_WORD_TEMPLATE = "Загаданное слово: %s";

    public String format(final WordleGame game) {
        Objects.requireNonNull(
                game, "Игра не задана."
        );

        if (!game.isFinished()) {
            throw new IllegalStateException("Игра не завершена.");
        }

        String resultLabel = game.isWon() ? VICTORY_LABEL : LOSE_LABEL;

        return resultLabel
                + LINE_SEPARATOR
                + SECRET_WORD_TEMPLATE.formatted(game.getSecret());
    }
}
