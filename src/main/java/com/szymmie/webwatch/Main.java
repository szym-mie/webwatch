package com.szymmie.webwatch;

import com.szymmie.webwatch.log.Log;
import com.szymmie.webwatch.log.PrettyLogFormatter;
import com.szymmie.webwatch.log.Tag;
import io.vertx.core.Vertx;

public class Main {
  public static final String TAG = Tag.of(Main.class);

  public static void main(String[] args) {
    Log.formatter = new PrettyLogFormatter();
    Log.i(Log.TAG, "using formatter", PrettyLogFormatter.TAG);

    Log.s(TAG, "deploying...");
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle())
      .onComplete(
        vid -> Log.v(TAG, "deploy success", MainVerticle.TAG),
        err -> Log.e(TAG, "deploy failed", err.getMessage())
      );
  }
}
