package ru.yandex.practicum.game.step;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.game.engine.LetterState;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.practicum.game.engine.LetterState.ABSENT;
import static ru.yandex.practicum.game.engine.LetterState.CORRECT;
import static ru.yandex.practicum.game.engine.LetterState.PRESENT;

class GameStepFormatterTest {
    private GameStepFormatter formatter;
    private static final String NL = System.lineSeparator();

    @BeforeEach
    void setUp() {
        formatter = new GameStepFormatter();
    }

    @Test
    void formatsGuessWithExactlyFiveSymbolsAndRemainingAttempts() {
        var guess = new GameStepResult.Guess(
                new LetterState[] {CORRECT, PRESENT, ABSENT, PRESENT, ABSENT});

        assertEquals("+^-^-" + NL + "Осталось попыток: 5", formatter.format(guess, 5));
    }

    @Test
    void formatsHintWordBeforeFeedback() {
        var hint = new GameStepResult.Hint(
                new LetterState[] {CORRECT, PRESENT, ABSENT, PRESENT, ABSENT}, "гонец");

        assertEquals("гонец" + NL + "+^-^-" + NL + "Осталось попыток: 0",
                formatter.format(hint, 0));
    }

    @Test
    void formatsWinningGuess() {
        var guess = new GameStepResult.Guess(
                new LetterState[] {CORRECT, CORRECT, CORRECT, CORRECT, CORRECT});

        assertEquals("+++++" + NL + "Осталось попыток: 0", formatter.format(guess, 0));
    }

    @Test
    void rejectsMissingResultOrLetterStates() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> formatter.format(null, 6)),
                () -> assertThrows(NullPointerException.class,
                        () -> formatter.format(new GameStepResult.Guess(null), 6)),
                () -> assertThrows(NullPointerException.class,
                        () -> formatter.format(new GameStepResult.Guess(
                                new LetterState[] {CORRECT, null, ABSENT, ABSENT, ABSENT}), 6))
        );
    }
}
