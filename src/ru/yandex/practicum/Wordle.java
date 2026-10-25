package ru.yandex.practicum;

import ru.yandex.practicum.game.dictionary.WordleDictionary;
import ru.yandex.practicum.game.dictionary.WordleDictionaryLoader;
import ru.yandex.practicum.game.engine.WordleGame;
import ru.yandex.practicum.game.formatter.GameResultFormatter;
import ru.yandex.practicum.game.step.GameStepFormatter;
import ru.yandex.practicum.game.step.GameStepResult;
import ru.yandex.practicum.logger.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */
public final class Wordle {

    private static final Path LOG_PATH = Path.of("log.txt");
    private static final Path DICTIONARY_PATH = Path.of("words_ru.txt");

    public static void main(String[] args) {
        try (
                Logger logger = new Logger(LOG_PATH);
                Scanner scanner = new Scanner(System.in)
        ) {
            WordleGame game = initializeGame(logger);

            gameLoop(game, scanner);

            if (game.isFinished()) {
                outputGameResult(game);
            }

        } catch (IOException e) {
            System.err.println(
                    "Failed to initialize game: " + e.getMessage()
            );
        }
    }

    private static void gameLoop(
            final WordleGame game,
            final Scanner scanner
    ) {
        GameStepFormatter formatter = new GameStepFormatter();

        while (!game.isFinished() && scanner.hasNextLine()) {
            String userInput = scanner.nextLine();

            try {
                GameStepResult stepResult;
                if (userInput.isEmpty()) {
                    stepResult = game.getHint();
                } else {
                    stepResult = game.evaluate(userInput);
                }

                String stepMessage = formatter.format(
                        stepResult,
                        game.getRemainingSteps()
                );

                System.out.println(stepMessage);

            } catch (Exception e) {

            }
        }
    }

    private static WordleGame initializeGame(
            final Logger logger
    ) throws IOException {
        WordleDictionaryLoader loader =
                new WordleDictionaryLoader(logger);

        WordleDictionary dictionary = loader.load(
                DICTIONARY_PATH
        );

        return new WordleGame(dictionary, logger);
    }

    private static void outputGameResult(final WordleGame game) {
        GameResultFormatter formatter = new GameResultFormatter();

        System.out.println(formatter.format(game));
    }
}
