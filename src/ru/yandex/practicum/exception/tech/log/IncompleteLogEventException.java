package ru.yandex.practicum.exception.tech.log;

import java.io.Serial;

public class IncompleteLogEventException extends LogEventException {
  @Serial
  private static final long serialVersionUID = 4384457169940646970L;

  public IncompleteLogEventException(String message) {
        super(message);
    }
}
