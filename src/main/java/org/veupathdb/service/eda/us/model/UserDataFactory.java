package org.veupathdb.service.eda.us.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.ArrayUtil;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.json.JsonUtil;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.service.eda.generated.model.AnalysisDescriptor;
import org.veupathdb.service.eda.generated.model.AnalysisSummary;
import org.veupathdb.service.eda.generated.model.AnalysisSummaryImpl;
import org.veupathdb.service.eda.us.Resources;
import org.veupathdb.service.eda.us.Utils;

public class UserDataFactory {

  private static final Logger LOG = LogManager.getLogger(UserDataFactory.class);

  private static final String SCHEMA_MACRO = "SCHEMA";

  private static final String INSERT_USER_SQL =
      "insert into " + SCHEMA_MACRO + "users " +
      " select %d as user_id, %d as is_guest, '{}' as preferences from dual" +
      " where not exists (select user_id from " + SCHEMA_MACRO + "users where user_id = %d)";

  private static final String READ_PREFS_SQL =
      "select preferences" +
      " from " + SCHEMA_MACRO + "users" +
      " where user_id = ?";

  private static final String WRITE_PREFS_SQL =
      "update " + SCHEMA_MACRO + "users" +
      " set preferences = ?" +
      " where user_id = ?";

  // constants for analysis table columns
  private static final String COL_ANALYSIS_ID = "analysis_id"; // varchar(50) not null,
  private static final String COL_USER_ID = "user_id"; // integer not null,
  private static final String COL_STUDY_ID = "study_id"; // varchar(50) not null,
  private static final String COL_DISPLAY_NAME = "display_name"; // varchar(50) not null,
  private static final String COL_DESCRIPTION = "description"; // varchar(4000),
  private static final String COL_CREATION_TIME = "creation_time"; // timestamp not null,
  private static final String COL_MODIFICATION_TIME = "modification_time"; // timestamp not null,
  private static final String COL_IS_PUBLIC = "is_public"; // integer not null,
  private static final String COL_NUM_FILTERS = "num_subsets"; // integer not null,
  private static final String COL_NUM_COMPUTATIONS = "num_computations"; // integer not null,
  private static final String COL_NUM_VISUALIZATIONS = "num_visualizations"; // integer not null,
  private static final String COL_DESCRIPTOR = "analysis_descriptor"; // clob,

  private static final String[] SUMMARY_COLS = {
      COL_ANALYSIS_ID, // varchar(50) not null,
      COL_USER_ID, // integer not null,
      COL_STUDY_ID, // varchar(50) not null,
      COL_DISPLAY_NAME, // varchar(50) not null,
      COL_DESCRIPTION, // varchar(4000),
      COL_CREATION_TIME, // timestamp not null,
      COL_MODIFICATION_TIME, // timestamp not null,
      COL_IS_PUBLIC, // integer not null,
      COL_NUM_FILTERS, // integer not null,
      COL_NUM_COMPUTATIONS, // integer not null,
      COL_NUM_VISUALIZATIONS // integer not null,
  };

  private static final String GET_ANALYSES_BY_USER_SQL =
      "select " + String.join(", ", SUMMARY_COLS) +
      " from " + SCHEMA_MACRO + "analysis" +
      " where " + COL_USER_ID + " = ?";

  private static final String[] DETAIL_COLS = ArrayUtil.concatenate(
      SUMMARY_COLS,
      new String[]{ COL_DESCRIPTOR }
  );

  private static final String GET_ANALYSIS_BY_ID_SQL =
      "select " + String.join(", ", DETAIL_COLS) +
      " from " + SCHEMA_MACRO + "analysis" +
      " where " + COL_ANALYSIS_ID + " = ?";

  private static String addSchema(String sqlConstant) {
    return sqlConstant.replace(SCHEMA_MACRO, Resources.getUserDbSchema());
  }

  public static void addUserIfAbsent(User user) {
    // need to use format vs prepared statement for first two macros since they are in a select
    String sql = String.format(
        addSchema(INSERT_USER_SQL),
        user.getUserID(),
        Resources.getUserPlatform().convertBoolean(user.isGuest()),
        user.getUserID());
    LOG.debug("Trying to insert user with SQL: " + sql);
    int newRows = new SQLRunner(Resources.getUserDataSource(), sql, "insert-user").executeUpdate();
    LOG.debug(newRows == 0 ? "User with ID " + user.getUserID() + " already present." : "New user inserted.");
  }

  public static String readPreferences(long userId) {
    return new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(READ_PREFS_SQL),
        "read-prefs"
    ).executeQuery(
        new Object[]{ userId },
        new Integer[]{ Types.BIGINT },
        rs -> rs.next()
          ? Resources.getUserPlatform().getClobData(rs, "preferences")
          : "{}"
    );
  }

  public static void writePreferences(long userId, String prefsObject) {
    new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(WRITE_PREFS_SQL),
        "write-prefs"
    ).executeStatement(
        new Object[]{ prefsObject, userId },
        new Integer[]{ Types.CLOB, Types.BIGINT }
    );
  }

  public static List<AnalysisSummary> getAnalysisSummaries(long userId) {
    return new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(GET_ANALYSES_BY_USER_SQL),
        "analysis-summaries"
    ).executeQuery(
        new Object[]{ userId },
        new Integer[]{ Types.BIGINT },
        rs -> {
          List<AnalysisSummary> list = new ArrayList<>();
          while (rs.next()) {
            AnalysisSummary sum = new AnalysisSummaryImpl();
            populateSummaryFields(sum, rs);
            list.add(sum);
          }
          return list;
        }
    );
  }

  public static AnalysisDetailWithUser getAnalysisById(String analysisId) {
    return new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(GET_ANALYSIS_BY_ID_SQL),
        "analysis-detail"
    ).executeQuery(
        new Object[]{ analysisId },
        new Integer[]{ Types.VARCHAR },
        rs -> {
          if (!rs.next()) {
            throw new NotFoundException();
          }
          AnalysisDetailWithUser analysis = new AnalysisDetailWithUser(rs);
          if (rs.next()) {
            throw new IllegalStateException("More than one analysis found with ID: " + analysisId);
          }
          return analysis;
        }
    );
  }

  private static void populateSummaryFields(AnalysisSummary analysis, ResultSet rs) throws SQLException {
    analysis.setAnalysisId(rs.getString(COL_ANALYSIS_ID)); // varchar(50) not null,
    analysis.setStudyId(rs.getString(COL_STUDY_ID)); // varchar(50) not null,
    analysis.setDisplayName(rs.getString(COL_DISPLAY_NAME)); // varchar(50) not null,
    analysis.setDescription(rs.getString(COL_DESCRIPTION)); // varchar(4000),
    analysis.setCreationTime(Utils.getDateString(rs.getTimestamp(COL_CREATION_TIME))); // timestamp not null,
    analysis.setModificationTime(Utils.getDateString(rs.getTimestamp(COL_MODIFICATION_TIME))); // timestamp not null,
    analysis.setIsPublic(Resources.getUserPlatform().getBooleanValue(rs, COL_IS_PUBLIC, false)); // integer not null,
    analysis.setNumFilters(rs.getInt(COL_NUM_FILTERS)); // integer not null,
    analysis.setNumComputations(rs.getInt(COL_NUM_COMPUTATIONS)); // integer not null,
    analysis.setNumVisualizations(rs.getInt(COL_NUM_VISUALIZATIONS)); // integer not null,
  }

  static void populateDetailFields(AnalysisDetailWithUser analysis, ResultSet rs) throws SQLException {
    analysis.setUserId(rs.getLong(COL_USER_ID));
    UserDataFactory.populateSummaryFields(analysis, rs);
    try {
      AnalysisDescriptor descriptor = JsonUtil.Jackson.readValue(
          Resources.getUserPlatform().getClobData(rs, COL_DESCRIPTOR), AnalysisDescriptor.class);
      analysis.setDescriptor(descriptor);
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException("Descriptor JSON for analysis " +
          analysis.getAnalysisId() + " could not be mapped to an AnalysisDescriptor object.", e);
    }
  }

  public static void insertAnalysis(AnalysisDetailWithUser analysis) {
  }

  public static void updateAnalysis(AnalysisDetailWithUser analysis) {
  }

  public static void deleteAnalyses(String... idsToDelete) {
  }
}
