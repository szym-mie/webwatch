package com.szymmie.webwatch.log;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class PlainLogFormatter implements LogFormatter {
  public PlainLogFormatter() {}

  private static final DateTimeFormatter timeFormatter =
    DateTimeFormatter.ISO_INSTANT;

  @Override
  public void out(long time, char type, String[] items) {
    Instant instant = Instant.ofEpochMilli(time);
    StringBuilder logBuilder = new StringBuilder();
    logBuilder
      .append(timeFormatter.format(instant))
      .append(" [")
      .append(type)
      .append("] ");
    for (String item : items) {
      logBuilder.append(item);
      logBuilder.append(" ");
    }
    System.out.println(logBuilder);
  }

  public static final String TAG = Tag.of(PlainLogFormatter.class);
}
