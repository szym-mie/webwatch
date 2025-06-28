package com.szymmie.webwatch.log;

public interface LogFormatter {
  void out(long time, char type, String[] items);
}
