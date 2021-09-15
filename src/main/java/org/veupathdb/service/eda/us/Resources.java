package org.veupathdb.service.eda.us;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.platform.Oracle;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.server.ContainerResources;
import org.veupathdb.lib.container.jaxrs.utils.db.DbManager;
import org.veupathdb.service.eda.us.service.PublicDataService;
import org.veupathdb.service.eda.us.service.UserService;
import org.veupathdb.service.eda.us.stubdb.StubDb;

import javax.sql.DataSource;

import static org.gusdb.fgputil.runtime.Environment.getOptionalVar;

/**
 * Service Resource Registration.
 *
 * This is where all the individual service specific resources and middleware
 * should be registered.
 */
public class Resources extends ContainerResources {

  private static final Logger LOG = LogManager.getLogger(Resources.class);

  private static final boolean DEVELOPMENT_MODE =
      Boolean.valueOf(getOptionalVar("DEVELOPMENT_MODE", "true"));

  private static final boolean USE_IN_MEMORY_TEST_DATABASE =
      Boolean.valueOf(getOptionalVar("USE_IN_MEMORY_TEST_DATABASE", "false"));

  private static final boolean SKIP_USER_VALIDATION =
      Boolean.valueOf(getOptionalVar("SKIP_USER_VALIDATION", "false"));

  public Resources(Options opts) {
    super(opts);

    // init user DB if options require it
    if (!USE_IN_MEMORY_TEST_DATABASE || !SKIP_USER_VALIDATION) {
      DbManager.initUserDatabase(opts);
    }

    // init account DB if options require it
    if (!SKIP_USER_VALIDATION) {
      DbManager.initAccountDatabase(opts);
      enableAuth();
    }

    if (DEVELOPMENT_MODE) {
      enableJerseyTrace();
    }

    LOG.info("User validation " + (SKIP_USER_VALIDATION ? "disabled" : "enabled"));

    LOG.info(USE_IN_MEMORY_TEST_DATABASE ? "Using in-memory DB for data storage" :
        "Using user DB for data storage with connection URL: " +
        DbManager.getInstance().getUserDatabase().getConfig().getConnectionUrl());
  }

  public static DataSource getUserDataSource() {
    return USE_IN_MEMORY_TEST_DATABASE
      ? StubDb.getDataSource()
      : DbManager.userDatabase().getDataSource();
  }

  public static String getUserDbSchema() {
    return USE_IN_MEMORY_TEST_DATABASE ? "" : "edauser.";
  }

  public static DBPlatform getUserPlatform() {
    return new Oracle();
  }

  /**
   * Returns an array of JaxRS endpoints, providers, and contexts.
   *
   * Entries in the array can be either classes or instances.
   */
  @Override
  protected Object[] resources() {
    return new Object[] {
      UserService.class,
      PublicDataService.class
    };
  }
}
