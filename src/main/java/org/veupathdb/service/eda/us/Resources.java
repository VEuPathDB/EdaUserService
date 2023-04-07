package org.veupathdb.service.eda.us;

import java.util.Map;
import java.util.Optional;

import jakarta.ws.rs.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.platform.Oracle;
import org.gusdb.fgputil.runtime.ProjectSpecificProperties;
import org.gusdb.fgputil.runtime.ProjectSpecificProperties.PropertySpec;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.server.ContainerResources;
import org.veupathdb.lib.container.jaxrs.utils.db.DbManager;
import org.veupathdb.service.eda.us.service.ImportAnalysisService;
import org.veupathdb.service.eda.us.service.MetricsService;
import org.veupathdb.service.eda.us.service.PublicDataService;
import org.veupathdb.service.eda.us.service.UserService;

import javax.sql.DataSource;

import static org.gusdb.fgputil.runtime.Environment.getOptionalVar;
import static org.gusdb.fgputil.runtime.Environment.getRequiredVar;
import static org.gusdb.fgputil.runtime.ProjectSpecificProperties.PropertySpec.required;

/**
 * Service Resource Registration.
 *
 * This is where all the individual service specific resources and middleware
 * should be registered.
 */
public class Resources extends ContainerResources {

  private static final Logger LOG = LogManager.getLogger(Resources.class);

  private static final boolean DEVELOPMENT_MODE =
      Boolean.parseBoolean(getOptionalVar("DEVELOPMENT_MODE", "true"));

  public static final String DATASET_ACCESS_SERVICE_URL = getRequiredVar("DATASET_ACCESS_SERVICE_URL");

  private static final String USER_SCHEMA_PROP = "USER_SCHEMA";
  private static Map<String,String> SCHEMA_MAP;

  public Resources(Options opts) {
    super(opts);

    // check for valid project-specific props
    SCHEMA_MAP = new ProjectSpecificProperties<>(
        new PropertySpec[] { required(USER_SCHEMA_PROP) },
        map -> {
          // add trailing '.' to schema names for convenience later
          String rawSchemaName = map.get(USER_SCHEMA_PROP);
          return rawSchemaName + (rawSchemaName.endsWith(".") ? "" : ".");
        }
    ).toMap();
    LOG.info("Schema map: " + FormatUtil.prettyPrint(SCHEMA_MAP, FormatUtil.Style.MULTI_LINE));

    // initialize auth and required DBs
    DbManager.initUserDatabase(opts);
    DbManager.initAccountDatabase(opts);
    enableAuth();

    if (DEVELOPMENT_MODE) {
      enableJerseyTrace();
    }

    LOG.info("Using user DB for data storage with connection URL: " +
        DbManager.getInstance().getUserDatabase().getConfig().getConnectionUrl());
  }

  public static DataSource getUserDataSource() {
    return DbManager.userDatabase().getDataSource();
  }

  public static DataSource getAccountsDataSource() { return DbManager.accountDatabase().getDataSource(); }

  public static String getUserDbSchema(String projectId) {
    if (!SCHEMA_MAP.containsKey(projectId)) {
      throw new NotFoundException("Invalid project ID: " + projectId);
    }
    return SCHEMA_MAP.get(projectId);
  }

  public static DBPlatform getUserPlatform() {
    return new Oracle();
  }

  public static String getMetricsReportSchema() {
    return Optional.ofNullable(System.getenv("USAGE_METRICS_SCHEMA")).orElse("usagemetrics.");
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
      PublicDataService.class,
      ImportAnalysisService.class,
      MetricsService.class
    };
  }
}
