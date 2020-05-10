package org.veupathdb.service.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.veupathdb.service.demo.config.Options;
import org.veupathdb.service.demo.container.middleware.AuthFilter;
import org.veupathdb.service.demo.container.middleware.Log4JFilter;
import org.veupathdb.service.demo.container.middleware.RequestIdFilter;
import org.veupathdb.service.demo.container.utils.Cli;
import org.veupathdb.service.demo.container.utils.DbManager;
import org.veupathdb.service.demo.container.utils.Log;
import org.veupathdb.service.demo.service.HelloWorld;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.UriBuilder;

import java.io.IOException;

import static java.lang.String.format;
import static org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory.createHttpServer;
import static org.veupathdb.service.demo.container.utils.Errors.silence;

@ApplicationPath("/")
public class Main extends ResourceConfig {
  private static final int DEFAULT_PORT = 8080;

  private static Logger log;

  public Main(Options opts, DatabaseInstance acctDb) {

    super(
      JacksonFeature.class,
      RequestIdFilter.class,
      Log4JFilter.class,

      // Endpoint Implementations
      HelloWorld.class
    );

    // Register middleware types that require dependencies.
    register(new AuthFilter(opts, acctDb));
  }

  public static void main(String[] args) throws IOException {
    // Configure Log4J and route all logging through it.
    Log.initialize();
    log = LogManager.getLogger(Main.class);

    final var opts = Cli.ParseCLI(args, Options.getInstance());
    validateOptions(opts);

    final var acctDb = DbManager.initAccountDatabase(opts);
    final var port = opts.getServerPort().orElse(DEFAULT_PORT);
    final var server = createHttpServer(
      UriBuilder.fromUri("//0.0.0.0").port(port).build(),
      new Main(opts, acctDb));

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Server shutting down.");
      server.shutdownNow();
      silence(acctDb::close);
    }));

    server.start();
    log.info(format("Server started.  Listening on port %d.", port));
  }

  /**
   * Fail fast: we know we need the AUTH_SECRET environment variable as well
   * as the database settings for this service.  If your service does not
   * require authentication, remove this check to remove the config
   * requirements.
   */
  static void validateOptions(Options opts) {
    var ok = true;
    if (opts.getAuthSecretKey().isEmpty()) {
      ok = false;
      log.error("Missing required auth secret key parameter.");
    }
    if (opts.getJdbcUrl().isEmpty()) {
      ok = false;
      log.error("Missing required database connection URL parameter.");
    }
    if (opts.getDbUser().isEmpty()) {
      ok = false;
      log.error("Missing required database username parameter.");
    }
    if (opts.getDbPass().isEmpty()) {
      ok = false;
      log.error("Missing required database password parameter.");
    }

    if (!ok) {
      log.error("Use --help to view required parameters.");
      System.exit(1);
    }
  }
}
