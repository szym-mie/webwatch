package com.szymmie.webwatch;

import com.szymmie.webwatch.html.Html;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Document {
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

  private final Instant createdAt;
  private final byte[] bytes;
  private final Document previousDocument;

  public Document(Instant createdAt, byte[] bytes, Document previousDocument) {
    this.createdAt = createdAt;
    this.bytes = bytes;
    this.previousDocument = previousDocument;
  }

  public boolean isNewer() {
    if (previousDocument == null)
      return true;

    if (createdAt.compareTo(previousDocument.createdAt) > 0) {
      if (bytes.length == previousDocument.bytes.length) {
        for (int i = 0; i < bytes.length; i++) {
          if (bytes[i] != previousDocument.bytes[i])
            return true;
        }
      }
    }

    return false;
  }

  private static String formatInstant(Instant instant) {
    return instant.atZone(ZoneId.systemDefault())
      .format(DATE_TIME_FORMATTER);
  }

  public String getChangeTime() {
    return formatInstant(createdAt);
  }

  public String getChangeMessage() {
    if (previousDocument != null) {
      int lengthDifference = bytes.length - previousDocument.bytes.length;
      return lengthDifference > 0 ?
        "+ " + lengthDifference + " bytes" :
        "- " + lengthDifference + " bytes";
    } else {
      return "init";
    }
  }

  public String toHtml() {
    String change = getChangeTime() + " | " + getChangeMessage();
    return Html.pre("document change:", change);
  }
}
