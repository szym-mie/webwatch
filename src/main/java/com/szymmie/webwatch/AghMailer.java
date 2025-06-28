package com.szymmie.webwatch;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;

public class AghMailer {
  private final String mailUsername;
  private final MailClient mailClient;

  private AghMailer(Vertx vertx, String username, String password) {
    MailConfig mailConfig = new MailConfig();
    mailConfig.setSsl(true);
    mailConfig.setHostname("poczta.agh.edu.pl");
    mailConfig.setPort(465);
    mailConfig.setUsername(username);
    mailConfig.setPassword(password);

    mailUsername = username;
    mailClient = MailClient.create(vertx, mailConfig);
  }

  public Future<MailResult> send(String subject, String toUsername, String html) {
    MailMessage mail = new MailMessage();
    mail.setSubject(subject);
    mail.setFrom(mailUsername);
    mail.setTo(toUsername);
    mail.setHtml(html);
    mail.addHeader("Content-Type", "text/html; charset=UTF-8");
    return mailClient.sendMail(mail);
  }

  public static AghMailer create(Vertx vertx, String username, String password) {
    return new AghMailer(vertx, username, password);
  }
}
