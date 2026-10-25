package ru.yandex.practicum.game.step;

import ru.yandex.practicum.game.engine.LetterState;

import java.util.Map;
import java.util.Objects;

public final class GameStepFormatter {
    private static final Map<LetterState, Character> CONSOLE_SYMBOLS = Map.of(
            LetterState.ABSENT, '-',
            LetterState.PRESENT, '^',
            LetterState.CORRECT, '+'
    );
    private static final String LINE_SEPARATOR = System.lineSeparator();

    public String format(GameStepResult result) {
        Objects.requireNonNull(
                result, "Result of step must not be null"
        );

        return switch (result) {
            case GameStepResult.Guess guess -> formatLetterStates(guess.state());
            case GameStepResult.Hint hint -> {
                String guessWord = Objects.requireNonNull(
                        hint.guess(), "Guess word must not be null"
                );
                yield guessWord +
                        LINE_SEPARATOR +
                        formatLetterStates(hint.state());
            }
        };
    }

    private String formatLetterStates(LetterState[] states) {
        Objects.requireNonNull(
                states, "States of guess word must not be null"
        );

        StringBuilder resultLine = new StringBuilder();
        for (LetterState state : states) {
            resultLine.append(CONSOLE_SYMBOLS.get(state));
        }
        return resultLine.toString();
    }
}
