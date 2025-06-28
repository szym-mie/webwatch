package com.szymmie.webwatch.log;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PrettyLogFormatter implements LogFormatter {
  public PrettyLogFormatter() {}

  private static final DateTimeFormatter timeFormatter =
    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS");

  @Override
  public void out(long time, char type, String[] items) {
    Instant instant = Instant.ofEpochMilli(time);
    ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
    StringBuilder logBuilder = new StringBuilder();
    logBuilder
      .append(timeFormatter.format(zonedDateTime))
      .append(" ")
      .append(formatType(type))
      .append(" ");
    for (String item : items) {
      logBuilder.append(item);
      logBuilder.append(" ");
    }
    System.out.println(logBuilder);
  }

  private static String formatType(char type) {
    return switch (type) {
      case 'i' -> "\033[97;44m i \033[0m";
      case 's' -> "\033[93m ~ \033[0m";
      case 'v' -> "\033[92m v \033[0m";
      case 't' -> "\033[95m***\033[0m";
      case 'd' -> "\033[94m > \033[0m";
      case 'c' -> "\033[96m = \033[0m";
      case 'w' -> "\033[97;43m w \033[0m";
      case 'e' -> "\033[97;41m e \033[0m";
      default -> "???";
    };
  }

  public static final String TAG = Tag.of(PrettyLogFormatter.class);
}
