package ru.yandex.practicum.exception.tech.log;

import ru.yandex.practicum.exception.tech.TechException;

import java.io.Serial;

public class LogEventException extends TechException {
  @Serial
  private static final long serialVersionUID = 7695136202926110489L;

  public LogEventException(String message) {
    super(message);
  }
}
