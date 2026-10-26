package ru.yandex.practicum;

import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.exception.WordleSystemException;
import ru.yandex.practicum.game.dictionary.WordleDictionary;
import ru.yandex.practicum.game.dictionary.WordleDictionaryLoader;
import ru.yandex.practicum.game.engine.WordleGame;
import ru.yandex.practicum.game.formatter.GameResultFormatter;
import ru.yandex.practicum.game.step.GameStepFormatter;
import ru.yandex.practicum.game.step.GameStepResult;
import ru.yandex.practicum.logger.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
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
        try (Logger logger = new Logger(LOG_PATH)) {
            try (Scanner scanner = new Scanner(System.in)) {
                WordleGame game = initializeGame(logger);

                gameLoop(game, scanner);

                if (game.isFinished()) {
                    outputGameResult(game);
                }
            } catch (Exception e) {
                logSystemError(logger, e);
            }
        } catch (Exception e) {
            logEmergencyError(e);
        }
    }

    private static void gameLoop(
            final WordleGame game,
            final Scanner scanner
    ) throws IOException {
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

            } catch (GameException e) {
                System.out.println(e.getMessage());
            }
        }

        if (scanner.ioException() != null) {
            throw new IOException("Failed to read user input", scanner.ioException());
        }
    }

    private static WordleGame initializeGame(
            final Logger logger
    ) throws WordleSystemException {
        WordleDictionaryLoader loader =
                new WordleDictionaryLoader(logger);

        WordleDictionary dictionary = loader.load(
                DICTIONARY_PATH
        );

        return new WordleGame(dictionary, logger);
    }

    private static void logSystemError(
            final Logger logger,
            final Exception error
    ) {
        try {
            StringWriter stackTrace = new StringWriter();
            try (PrintWriter writer = new PrintWriter(stackTrace)) {
                error.printStackTrace(writer);
            }

            logger.atError()
                    .addKeyValue("exception", error.getClass().getName())
                    .addKeyValue("stack_trace", stackTrace.toString())
                    .log("Game stopped because of a system error");
        } catch (RuntimeException loggingError) {
            loggingError.addSuppressed(error);
            throw loggingError;
        }
    }

    private static void logEmergencyError(final Exception error) {
        try {
            Path emergencyLog = Files.createTempFile("wordle-error-", ".log");
            try (Logger logger = new Logger(emergencyLog)) {
                logSystemError(logger, error);
            }
        } catch (Exception e) {
            System.err.println("Game was stopped: saving log journal is failed");
        }
    }

    private static void outputGameResult(final WordleGame game) {
        GameResultFormatter formatter = new GameResultFormatter();

        System.out.println(formatter.format(game));
    }
}
