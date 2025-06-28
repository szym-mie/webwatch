package com.szymmie.webwatch.log;

public class Log {
  public static LogFormatter formatter = new PlainLogFormatter();

  /**
   * Log message as info
   * @param items Items to log
   */
  public static void i(String... items) {
    formatter.out(System.currentTimeMillis(), 'i', items);
  }

  /**
   * Log message - start of service, deployment, thread, etc
   * @param items Items to log
   */
  public static void s(String... items) {
    formatter.out(System.currentTimeMillis(), 's', items);
  }

  /**
   * Log message - action completed
   * @param items Items to log
   */
  public static void v(String... items) {
    formatter.out(System.currentTimeMillis(), 'v', items);
  }

  /**
   * Log message - timeout, responsiveness degradation
   * @param items Items to log
   */
  public static void t(String... items) {
    formatter.out(System.currentTimeMillis(), 't', items);
  }

  /**
   * Log message - incoming data, message
   * @param items Items to log
   */
  public static void d(String... items) {
    formatter.out(System.currentTimeMillis(), 'd', items);
  }

  /**
   * Log config option being used
   * @param items Items to log
   */
  public static void c(String... items) {
    formatter.out(System.currentTimeMillis(), 'c', items);
  }

  /**
   * Log message as warning
   * @param items Items to log
   */
  public static void w(String... items) {
    formatter.out(System.currentTimeMillis(), 'w', items);
  }

  /**
   * Log message as non-fatal error
   * @param items Items to log
   */
  public static void e(String... items) {
    formatter.out(System.currentTimeMillis(), 'e', items);
  }

  public static final String TAG = Tag.of(Log.class);
}
