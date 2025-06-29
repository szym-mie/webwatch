package com.szymmie.webwatch;

import com.szymmie.webwatch.html.Html;
import com.szymmie.webwatch.log.Log;
import com.szymmie.webwatch.log.PrettyLogFormatter;
import com.szymmie.webwatch.log.Tag;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {
  private static final long INIT_DELAY = 10_000;
  private static final long DELAY = 20_000;
  private static final String LOCAL_ADDRESS = "127.0.0.1:9090";
  private static final String HOST = "galaxy.agh.edu.pl";
  private static final String PATH = "/~luke/sr-oceny-2025.pdf";

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

  public Instant lastUpdated = null;
  public Set<String> emails = new HashSet<>();
  public List<Document> documents = new LinkedList<>();

  private AghMailer mailer = null;
  private RedisAPI redis = null;

  @Override
  public void start(Promise<Void> startPromise) {
    Log.formatter = new PrettyLogFormatter();
    Log.i(Log.TAG, "using formatter", PrettyLogFormatter.TAG);

    Log.s(TAG, "deploying...");

    ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
    configRetriever.getConfig().onComplete(ar -> {
      if (ar.failed()) {
        startPromise.fail("could not retrieve config");
      } else {
        Log.i(TAG, "retrieved config");
        JsonObject config = ar.result();

        String username = config.getString("MAIL_USERNAME");
        String password = config.getString("MAIL_PASSWORD");
        Log.i(TAG, "mailer config");
        Log.c(TAG, "username", username);
        Log.c(TAG, "password", "*".repeat(password.length()));
        mailer = AghMailer.create(vertx, username, password);

        String url = config.getString("REDIS_URL");
        Log.i(TAG, "redis config");
        Log.c(TAG, "url", url);
        Log.s(TAG, "connecting on redis");
        Redis.createClient(vertx, url)
          .connect()
          .onComplete(conn -> {
            if (conn.succeeded()) {
              Log.v(TAG, "connected to redis");
              redis = RedisAPI.api(conn.result());
              redis.lrange("emails", "0", "-1")
                .onSuccess(res -> {
                  emails = res.stream()
                    .map(Response::toString)
                    .collect(Collectors.toSet());
                  Log.v(TAG, "loaded " + emails.size() + " emails");
                })
                .onFailure(err ->
                  Log.e(TAG, "failed to read emails from redis", err.getMessage())
                );
            } else {
              startPromise.fail(conn.cause());
            }
          });
      }
    });

    WebClientOptions webClientOptions = new WebClientOptions();
    webClientOptions.setUserAgent("Web-Watch/1.0.0");
    webClientOptions.setLocalAddress(LOCAL_ADDRESS);
    WebClient webClient = WebClient.create(vertx);
    Log.c(TAG, "client.local_address", LOCAL_ADDRESS);

    Router router = Router.router(vertx);
    router.get("/").produces("text/html").handler(this::handleIndex);
    router.get("/join").produces("text/html").handler(this::handleJoin);
    router.get("/quit").produces("text/html").handler(this::handleQuit);
    Log.i(TAG, "registered routes:");
    router.getRoutes().forEach(route -> Log.c(TAG, route.getPath()));

    vertx.createHttpServer().requestHandler(router)
      .listen(8888).onComplete(http -> {
        if (http.succeeded()) {
          startPromise.complete();
          Log.v(TAG, "listen on :8888");
        } else {
          startPromise.fail(http.cause());
        }
      });
    vertx.setPeriodic(INIT_DELAY, DELAY, _timerId -> {
      Log.t(TAG, "query document");
      webClient.get(HOST, PATH)
        .send()
        .onComplete(req -> {
          if (req.succeeded()) {
            lastUpdated = Instant.now();
            handleDocumentRequest(req.result());
          } else {
            Log.e(TAG, "req failed", req.cause().getMessage());
          }
        });
    });
  }

  private Optional<Instant> getLastUpdated() {
    return Optional.ofNullable(lastUpdated);
  }

  private static String formatInstant(Instant instant) {
    return instant.atZone(ZoneId.systemDefault())
      .format(DATE_TIME_FORMATTER);
  }

  private void handleIndex(RoutingContext ctx) {
    String lastUpdateText = getLastUpdated()
      .map(MainVerticle::formatInstant)
      .orElse("wait");

    String html = Html.doc("Web-Watch /",
      Html.h3("Web-Watch 1.0.0"),
      Html.h5("watching: "),
      Html.pre("Host: " + HOST, "Path: " + PATH),
      Html.h5("changes: "),
      Html.table(
        Map.of(
          "time", Document::getChangeTime,
          "changes", Document::getChangeMessage
        ),
        documents
      ),
      Html.h5("emails: "),
      Html.table(
        Map.of(
          "email", v -> v,
          "actions", v -> Html.a("/quit?email=" + v, "quit")
        ),
        emails
      ),
      Html.h5("register email: (@student.agh.edu.pl)"),
      Html.form("/join", "register",
        Html.field("username", "text", "name")
      ),
      Html.h5("info: "),
      Html.pre(
        "local_address = " + LOCAL_ADDRESS,
        "last_update   = " + lastUpdateText,
        "update_delay  = " + DELAY / 1_000
      )
    );
    ctx.response().end(html);
  }

  private void handleJoin(RoutingContext ctx) {
    HttpServerRequest req = ctx.request();
    String name = req.getParam("name");
    if (name == null || name.contains("@") || name.contains(".")) {
      String html = Html.doc("Web-Watch /join",
        Html.h3("Error"),
        Html.p("email name cannot contain '@' or '.'"),
        Html.a("/", "to homepage")
      );
      Log.w(TAG, "could not register " + name);
      ctx.response().end(html);
    } else {
      String email = name + "@student.agh.edu.pl";
      Log.d(TAG, "registered " + email);
      emails.add(email);
      String pageHtml = Html.doc("Web-Watch /join",
        Html.h3("Joined as " + email),
        Html.a("/", "to homepage")
      );
      redis.lpush(List.of("emails", email));
      String emailHtml =
        Html.h3("Web-Watch - you subscribed to watch changes") +
        Html.pre("Host: " + HOST, "Path: " + PATH);
      String subject = "[notification] - Web-Watch - joined";
      mailer.send(subject, email, emailHtml).onComplete(mre -> {
        if (mre.succeeded()) {
          Log.v(TAG, "sent email to " + email);
        } else {
          Log.w(TAG, "email not sent to " + email);
        }
      });
      ctx.response().end(pageHtml);
    }
  }

  private void handleQuit(RoutingContext ctx) {
    HttpServerRequest req = ctx.request();
    String email = req.getParam("email");
    if (email == null || !emails.contains(email)) {
      String html = Html.doc("Web-Watch /quit",
        Html.h3("No such " + email),
        Html.a("/", "to homepage")
      );
      ctx.response().end(html);
    } else {
      emails.remove(email);
      String html = Html.doc("Web-Watch /quit",
        Html.h3("Quit " + email),
        Html.a("/", "to homepage")
      );
      ctx.response().end(html);
    }
  }

  private void handleDocumentRequest(HttpResponse<Buffer> res) {
    Buffer buf = res.body();
    Log.d(TAG, "got document of length " + buf.length());
    Document oldDocument = !documents.isEmpty() ? documents.get(0) : null;
    Document newDocument = new Document(
      Instant.now(),
      buf.getBytes(),
      oldDocument
    );
    if (newDocument.isNewer()) {
      documents.add(0, newDocument);
      Log.v(TAG, "upserted document");

      String subject = "[notification] - Web-Watch - change detected";
      String html =
        Html.h3("Web-Watch - notification") +
        Html.h5("We detected a change:") +
        Html.pre("Host: " + HOST, "Path: " + PATH, "---") +
        newDocument.toHtml();
      Log.s(TAG, "sending " + emails.size() + " emails");
      for (String email : emails) {
        mailer.send(subject, email, html).onComplete(mre -> {
          if (mre.succeeded()) {
            Log.v(TAG, "sent email to " + email);
          } else {
            Log.w(TAG, "email not sent to " + email);
          }
        });
      }
    } else {
      Log.v(TAG, "document up-to-date");
    }
  }

  public static final String TAG = Tag.of(MainVerticle.class);
}
