package ru.yandex.practicum.game.step;

import ru.yandex.practicum.game.engine.LetterState;

public sealed interface GameStepResult
    permits GameStepResult.Hint, GameStepResult.Guess {
    LetterState[] state();

    record Guess(LetterState[] state) implements GameStepResult {}
    record Hint(LetterState[] state, String guess) implements GameStepResult {}
}
