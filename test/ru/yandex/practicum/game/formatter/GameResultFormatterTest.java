package ru.yandex.practicum.game.formatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.game.dictionary.WordleDictionary;
import ru.yandex.practicum.game.engine.WordleGame;

import java.io.PrintWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameResultFormatterTest {
    private GameResultFormatter formatter;
    private WordleGame game;

    @BeforeEach
    void setUp() throws Exception {
        formatter = new GameResultFormatter();
        PrintWriter log = new PrintWriter(System.out, true);
        game = new WordleGame(new WordleDictionary(List.of("герой", "гонец"), log), log);
    }

    @Test
    void revealsSecretAfterVictory() throws Exception {
        game.evaluate(game.getSecret());

        assertEquals("Вы победили! Поздравляем!" + System.lineSeparator()
                + "Загаданное слово: " + game.getSecret(), formatter.format(game));
    }

    @Test
    void revealsSecretAfterDefeat() throws Exception {
        String wrong = game.getSecret().equals("герой") ? "гонец" : "герой";
        for (int i = 0; i < 6; i++) {
            game.evaluate(wrong);
        }

        assertEquals("Вы проиграли." + System.lineSeparator()
                + "Загаданное слово: " + game.getSecret(), formatter.format(game));
    }

    @Test
    void rejectsUnfinishedGame() {
        assertThrows(IllegalStateException.class, () -> formatter.format(game));
    }

    @Test
    void rejectsMissingGame() {
        assertThrows(NullPointerException.class, () -> formatter.format(null));
    }
}
