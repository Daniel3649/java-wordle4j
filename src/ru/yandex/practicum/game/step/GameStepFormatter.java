package ru.yandex.practicum.game.step;

import ru.yandex.practicum.game.engine.LetterState;
import ru.yandex.practicum.game.engine.WordleGame;

import java.util.Map;
import java.util.Objects;

public final class GameStepFormatter {

    private static final Map<LetterState, Character> CONSOLE_SYMBOLS = Map.of(
            LetterState.ABSENT, '-',
            LetterState.PRESENT, '^',
            LetterState.CORRECT, '+'
    );

    private static final String LINE_SEPARATOR =
            System.lineSeparator();

    private static final String REMAINING_STEPS_TEMPLATE =
            "Осталось попыток: %d";

    public String format(
            final GameStepResult result,
            final int remainingSteps
    ) {
        Objects.requireNonNull(
                result, "Результат хода не задан."
        );

        String formattedResult = switch (result) {
            case GameStepResult.Guess guess ->
                    formatLetterStates(guess.state());

            case GameStepResult.Hint hint ->
                    hint.guess()
                            + LINE_SEPARATOR
                            + formatLetterStates(hint.state());
        };

        return formattedResult
                + LINE_SEPARATOR
                + REMAINING_STEPS_TEMPLATE.formatted(remainingSteps);
    }

    private static String formatLetterStates(
            final LetterState[] states
    ) {
        Objects.requireNonNull(
                states, "Состояния букв не заданы."
        );

        StringBuilder resultLine = new StringBuilder(states.length);

        for (LetterState state : states) {
            resultLine.append(
                    Objects.requireNonNull(
                            CONSOLE_SYMBOLS.get(state),
                            "Неизвестное состояние буквы: " + state
                    )
            );
        }

        return resultLine.toString();
    }
}
