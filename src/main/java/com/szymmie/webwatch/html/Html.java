package com.szymmie.webwatch.html;

import java.util.Map;
import java.util.function.Function;

public class Html {
  public static String doc(String title, String... children) {
    return "<!doctype html>\n" +
      "<html lang=\"en\">\n" +
      "<head>\n" +
      "  <meta charset=\"UTF-8\">\n" +
      "  <title>" + title + "</title>\n" +
      "</head>\n" +
      "<body>\n" + String.join("\n", children) + "\n</body>\n" +
      "</html>";
  }

  public static String h1(String text) {
    return "<h1>" + text + "</h1>";
  }

  public static String h3(String text) {
    return "<h3>" + text + "</h3>";
  }

  public static String h5(String text) {
    return "<h5>" + text + "</h5>";
  }

  public static String p(String text) {
    return "<p>" + text + "</p>";
  }

  public static String a(String href, String text) {
    return "<a href=\"" + href + "\">" + text + "</a>";
  }

  public static <V> String table(
    Map<String, Function<V, String>> columnMapping,
    Iterable<V> rows
  ) {
    StringBuilder columnNamesBuilder = new StringBuilder();
    StringBuilder rowsBuilder = new StringBuilder();

    for (String columnName : columnMapping.keySet()) {
      columnNamesBuilder
        .append("<th>")
        .append(columnName)
        .append("</th>");
    }

    for (V row : rows) {
      StringBuilder rowBuilder = new StringBuilder();
      for (Function<V, String> columnMapper : columnMapping.values()) {
        rowBuilder
          .append("<td>\n")
          .append(columnMapper.apply(row))
          .append("</td>\n");
      }
      rowsBuilder
        .append("<tr>\n")
        .append(rowBuilder)
        .append("</tr>\n");
    }

    return "<table>\n" +
      "<thead>\n<tr>" + columnNamesBuilder + "</tr>\n</thead>\n" +
      "<tbody>\n" + rowsBuilder + "\n</tbody>\n" +
      "</table>";
  }

  public static String form(String action, String submit, String... items) {
    return "<form action=\"" + action + "\">" +
      String.join("\n", items) + "\n" +
      "<input type=\"submit\" value=\"" + submit + "\"/></form>";
  }

  public static String field(String label, String type, String id) {
    return "<label for=\"" + id + "\">" + label + "</label>\n" +
      "<input type=\"" + type + "\" id=\"" + id + "\" name=\"" + id + "\"/>";
  }

  public static String pre(String... lines) {
    return "<pre>" + String.join("\n", lines) + "</pre>";
  }
}
