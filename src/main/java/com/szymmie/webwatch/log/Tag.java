package com.szymmie.webwatch.log;

public class Tag {
  public static String of(Class<?> owner) {
    StringBuilder idBuilder = new StringBuilder();
    String[] terms = owner.getName().split("\\.");
    String lastTerm = terms[terms.length - 1];
    for (int i = 0; i < terms.length - 1; i++) {
      idBuilder.append(terms[i].charAt(0));
      idBuilder.append(".");
    }
    idBuilder.append(lastTerm);
    return idBuilder.toString();
  }
}
