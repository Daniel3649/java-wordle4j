package ru.yandex.practicum.game.formatter;

import ru.yandex.practicum.game.engine.WordleGame;

import java.util.Objects;

public final class GameResultFormatter {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String VICTORY_LABEL = "You win. Congratulations!";
    private static final String LOSE_LABEL = "You lost";
    private static final String SECRET_WORD_TEMPLATE = "Secret word: %s";

    public String format(final WordleGame game) {
        Objects.requireNonNull(
                game, "Game must not be null"
        );

        if (!game.isFinished()) {
            throw new IllegalStateException("Game is not finished");
        }

        String resultLabel = game.isWon() ? VICTORY_LABEL : LOSE_LABEL;

        return resultLabel
                + LINE_SEPARATOR
                + SECRET_WORD_TEMPLATE.formatted(game.getSecret());
    }
}
