package ru.yandex.practicum.exception.tech.log;

import java.io.Serial;

public class LogEventAlreadyLoggedException extends LogEventException {
  @Serial
  private static final long serialVersionUID = -4460538116934912330L;

  public LogEventAlreadyLoggedException(String message) {
        super(message);
    }
}
