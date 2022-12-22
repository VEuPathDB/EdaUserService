package org.veupathdb.service.eda.us.model;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.ArrayUtil;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.service.eda.generated.model.*;
import org.veupathdb.service.eda.us.Resources;
import org.veupathdb.service.eda.us.Utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Performs all database operations for the user service
 */
public class UserDataFactory {

  private static final Logger LOG = LogManager.getLogger(UserDataFactory.class);

  private static final String SCHEMA_MACRO = "$SCHEMA$";
  private static final String TABLE_USERS = SCHEMA_MACRO + "users";
  private static final String TABLE_ANALYSIS = SCHEMA_MACRO + "analysis";

  // constants for analysis table columns
  private static final String COL_ANALYSIS_ID = "analysis_id"; // varchar(50) not null,
  private static final String COL_USER_ID = "user_id"; // integer not null,
  private static final String COL_STUDY_ID = "study_id"; // varchar(50) not null,
  private static final String COL_STUDY_VERSION = "study_version"; // varchar(50),
  private static final String COL_API_VERSION = "api_version"; // varchar(50),
  private static final String COL_DISPLAY_NAME = "display_name"; // varchar(50) not null,
  private static final String COL_DESCRIPTION = "description"; // varchar(4000),
  private static final String COL_CREATION_TIME = "creation_time"; // timestamp not null,
  private static final String COL_MODIFICATION_TIME = "modification_time"; // timestamp not null,
  private static final String COL_IS_PUBLIC = "is_public"; // integer not null,
  private static final String COL_NUM_FILTERS = "num_filters"; // integer not null,
  private static final String COL_NUM_COMPUTATIONS = "num_computations"; // integer not null,
  private static final String COL_NUM_VISUALIZATIONS = "num_visualizations"; // integer not null,
  private static final String COL_DESCRIPTOR = "analysis_descriptor"; // clob,
  private static final String COL_NOTES = "notes"; // clob,
  private static final String COL_PROVENANCE = "provenance"; // clob,


  private static final String[] SUMMARY_COLS = {
      COL_ANALYSIS_ID, // varchar(50) not null,
      COL_USER_ID, // integer not null,
      COL_STUDY_ID, // varchar(50) not null,
      COL_STUDY_VERSION, // varchar(50),
      COL_API_VERSION, // varchar(50),
      COL_DISPLAY_NAME, // varchar(50) not null,
      COL_DESCRIPTION, // varchar(4000),
      COL_CREATION_TIME, // timestamp not null,
      COL_MODIFICATION_TIME, // timestamp not null,
      COL_IS_PUBLIC, // integer not null,
      COL_NUM_FILTERS, // integer not null,
      COL_NUM_COMPUTATIONS, // integer not null,
      COL_NUM_VISUALIZATIONS, // integer not null,
      COL_PROVENANCE // clob,
  };

  private static final String[] DETAIL_COLS = ArrayUtil.concatenate(
      SUMMARY_COLS,
      new String[]{
          COL_DESCRIPTOR,
          COL_NOTES
      }
  );

  private static final Integer[] DETAIL_COL_TYPES = new Integer[] {
      Types.VARCHAR, // analysis_id
      Types.BIGINT, // user_id
      Types.VARCHAR, // study_id
      Types.VARCHAR, // study_version
      Types.VARCHAR, // api_version
      Types.VARCHAR, // display_name
      Types.VARCHAR, // description
      Types.TIMESTAMP, // creation_time
      Types.TIMESTAMP, // modification_time
      Resources.getUserPlatform().getBooleanType(), // is_public
      Types.INTEGER, // num_filters
      Types.INTEGER, // num_computations
      Types.INTEGER, // num_visualizations
      Types.CLOB, // provenance
      Types.CLOB, // descriptor
      Types.CLOB // notes
  };

  private final String _userSchema;

  public UserDataFactory(String projectId) {
    _userSchema = Resources.getUserDbSchema(projectId);
  }

  private String addSchema(String sqlConstant) {
    return sqlConstant.replace(SCHEMA_MACRO, _userSchema);
  }

  /***************************************************************************************
   *** Insert user
   **************************************************************************************/

  private static final String INSERT_USER_SQL =
      "insert into " + TABLE_USERS +
      " select %d as user_id, %d as is_guest, '{}' as preferences from dual" +
      " where not exists (select user_id from " + TABLE_USERS + " where user_id = %d)";

  public void addUserIfAbsent(User user) {
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

  /***************************************************************************************
   *** Read preferences
   **************************************************************************************/

  private static final String READ_PREFS_SQL =
      "select preferences" +
      " from " + TABLE_USERS +
      " where user_id = ?";

  public String readPreferences(long userId) {
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

  /***************************************************************************************
   *** Write preferences
   **************************************************************************************/

  private static final String WRITE_PREFS_SQL =
      "update " + TABLE_USERS +
      " set preferences = ?" +
      " where user_id = ?";

  public void writePreferences(long userId, String prefsObject) {
    new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(WRITE_PREFS_SQL),
        "write-prefs"
    ).executeStatement(
        new Object[]{ prefsObject, userId },
        new Integer[]{ Types.CLOB, Types.BIGINT }
    );
  }

  /***************************************************************************************
   *** Get user's analyses
   **************************************************************************************/

  private static final String GET_ANALYSES_BY_USER_SQL =
      "select " + String.join(", ", SUMMARY_COLS) +
      " from " + TABLE_ANALYSIS +
      " where " + COL_USER_ID + " = ?" +
      " order by " + COL_MODIFICATION_TIME + " desc";

  public List<AnalysisSummary> getAnalysisSummaries(long userId) {
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

  /***************************************************************************************
   *** Get single analysis
   **************************************************************************************/

  private static final String GET_ANALYSIS_BY_ID_SQL =
      "select " + String.join(", ", DETAIL_COLS) +
      " from " + TABLE_ANALYSIS +
      " where " + COL_ANALYSIS_ID + " = ?";

  public AnalysisDetailWithUser getAnalysisById(String analysisId) {
    try {
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
    catch (SQLRunnerException e) {
      // throw underlying exception if able
      throw e.getCause() != null && e.getCause() instanceof RuntimeException ? (RuntimeException)e.getCause() : e;
    }
  }

  /***************************************************************************************
   *** Insert analysis
   **************************************************************************************/

  private static final String INSERT_ANALYSIS_SQL =
      "insert into " + TABLE_ANALYSIS + " ( " +
        String.join(", ", DETAIL_COLS) +
      " ) values ( " +
        Arrays.stream(DETAIL_COLS).map(c -> "?").collect(Collectors.joining(", ")) +
      " ) ";

  public void insertAnalysis(AnalysisDetailWithUser analysis) {
    new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(INSERT_ANALYSIS_SQL),
        "insert-analysis"
    ).executeStatement(
        getAnalysisInsertValues(analysis),
        DETAIL_COL_TYPES
    );
  }

  /***************************************************************************************
   *** Update analysis
   **************************************************************************************/

  private static final String UPDATE_ANALYSIS_SQL =
      "update " + TABLE_ANALYSIS + " set " +
      Arrays.stream(DETAIL_COLS).map(c -> c + " = ?").collect(Collectors.joining(", ")) +
      " where " + COL_ANALYSIS_ID + " = ?";

  public void updateAnalysis(AnalysisDetailWithUser analysis) {
    int rowsUpdated = new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(UPDATE_ANALYSIS_SQL),
        "update-analysis"
    ).executeUpdate(
        ArrayUtil.concatenate(
            getAnalysisInsertValues(analysis),
            new Object[] { analysis.getAnalysisId() } // extra ? for analysis_id in where statement
        ),
        ArrayUtil.concatenate(
            DETAIL_COL_TYPES,
            new Integer[] { Types.VARCHAR } // extra ? for analysis_id in where statement
        )
    );
    if (rowsUpdated != 1) {
      throw new IllegalStateException("Updated " + rowsUpdated +
          " rows (should be 1) in DB when updated analysis with ID " + analysis.getAnalysisId());
    }
  }

  /***************************************************************************************
   *** Delete a set of analyses
   **************************************************************************************/

  private static final String IDS_MACRO_LIST_MACRO = "$MACRO_LIST$";
  private static final String DELETE_ANALYSES_SQL =
      "delete from " + TABLE_ANALYSIS +
      " where " + COL_ANALYSIS_ID + " IN  ( " + IDS_MACRO_LIST_MACRO + " )";

  public void deleteAnalyses(String... idsToDelete) {

    // check for valid number of IDs
    if (idsToDelete.length == 0) return;
    if (idsToDelete.length > 200) throw new BadRequestException("Too many IDS (max = 200)");

    // construct prepared statement SQL with the right number of macros for all the IDs
    String[] stringArr = new String[idsToDelete.length];
    Arrays.fill(stringArr, "?");
    String sql = DELETE_ANALYSES_SQL.replace(IDS_MACRO_LIST_MACRO, String.join(", ", stringArr));

    new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(sql),
        "delete-analyses"
    ).executeStatement(
        idsToDelete
    );
  }

  /***************************************************************************************
   *** Get public analyses
   **************************************************************************************/

  private static final String GET_PUBLIC_ANALYSES_SQL =
      "select " + String.join(", ", SUMMARY_COLS) +
      " from " + TABLE_ANALYSIS +
      " where " + COL_IS_PUBLIC + " = " + Resources.getUserPlatform().convertBoolean(true) +
      " order by " + COL_MODIFICATION_TIME + " desc";

  public List<AnalysisSummaryWithUser> getPublicAnalyses() {
    return new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(GET_PUBLIC_ANALYSES_SQL),
        "public-analysis-summaries"
    ).executeQuery(
        rs -> {
          List<AnalysisSummaryWithUser> list = new ArrayList<>();
          while (rs.next()) {
            AnalysisSummaryWithUser sum = new AnalysisSummaryWithUserImpl();
            sum.setUserId(rs.getLong(COL_USER_ID));
            populateSummaryFields(sum, rs);
            list.add(sum);
          }
          return list;
        }
    );
  }

  /***************************************************************************************
   *** Transfer guest analyses to logged in user
   **************************************************************************************/

  private static final String TRANSFER_GUEST_ANALYSES_SQL =
      "update " + TABLE_ANALYSIS +
      " set user_id = ?" +
      " where analysis_id in (" +
      "   select analysis_id" +
      "   from " + TABLE_ANALYSIS + " a, " + TABLE_USERS + " u" +
      "   where a.user_id = u.user_id" +
      "   and u.is_guest = " + Resources.getUserPlatform().convertBoolean(true) +
      "   and u.user_id = ?" +
      " )";

  public void transferGuestAnalysesOwnership(long fromGuestUserId, long toRegisteredUserId) {
    new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(TRANSFER_GUEST_ANALYSES_SQL),
        "transfer-analyses"
    ).executeStatement(
        new Object[]{ toRegisteredUserId, fromGuestUserId }
    );
  }

  /***************************************************************************************
   *** Read analysis metrics
   **************************************************************************************/
  public enum Imported {
    YES, NO
  }
  public enum IsGuest {
    YES(1), NO(0);
    private final int flag;
    IsGuest(int flag) { this.flag = flag;}
  }
  public List<String> getIgnoreInMetricsUserIds() {
    String sql = """
            select user_id from useraccounts.account_properties
            where key = 'ignore_in_metrics' and value = 'true'
            """;
    return new SQLRunner(
            Resources.getAccountsDataSource(),
            sql,
            "get-ignore-in-metrics-user-ids"
    ).executeQuery(
            rs -> {
              List<String> userIds = new ArrayList<>();
              while (rs.next()) userIds.add(Integer.toString(rs.getInt("USER_ID")));
              return userIds;
            }
    );
  }

  public List<StudyCount> readAnalysisCountsByStudy(LocalDate startDate, LocalDate endDate, String dateColumn, String ignoreUserIds, Imported imported) {
    String sqlTemplate = """
select count(analysis_id) as cnt, study_id
from %sanalysis a, %susers u
where %s > ? and %s <= ?
and a.user_id = u.user_id
and a.user_id NOT IN (%s)
%s
group by study_id
order by cnt desc
        """;
    String importClause = imported == Imported.YES ? "and provenance is not null" + System.lineSeparator() : "";
    String sql = String.format(sqlTemplate, _userSchema, _userSchema, dateColumn, dateColumn, ignoreUserIds, importClause);

    return new SQLRunner(
            Resources.getUserDataSource(),
            sql,
            "read-analysis-counts"
    ).executeQuery(
            new Object[]{ java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate) },
            new Integer[]{ Types.DATE, Types.DATE },
            rs -> {
              List<StudyCount> countPerStudy = new ArrayList<>();
              while (rs.next()) {
                int count = rs.getInt("CNT");
                String studyId = rs.getString("STUDY_ID");
                StudyCount sc = new StudyCountImpl();
                sc.setCount(count);
                sc.setStudyId(studyId);
                countPerStudy.add(sc);
              }
              return countPerStudy;
            }
    );
  }

  // collect a histogram of counts of number of users with a number of some object (eg analyses or filters) from the analysis table.
  // the objects are aggregated by the aggregateObjectsSql.  EG:  "count(analysis_id)" or "sum(num_filters)"
  public List<UsersObjectsCount> readObjectCountsByUserCounts(String aggregateObjectSql, LocalDate startDate, LocalDate endDate, String dateColumn, String ignoreIdsString, IsGuest isGuest) {
    String sqlTemplate = """
  select count(user_id) as user_cnt, objects
  from (
    select %s as objects, a.user_id
    from %sanalysis a, %susers u
    where %s > ? and %s <= ?
    and a.user_id = u.user_id
    and a.user_id NOT IN (%s)
    and is_guest = ?
    group by a.user_id
  )
  group by objects
  order by objects desc
  """;

    String sql = String.format(sqlTemplate, aggregateObjectSql, _userSchema, _userSchema, dateColumn, dateColumn, ignoreIdsString);

    return new SQLRunner(
            Resources.getUserDataSource(),
            sql,
            "read-user-object-counts"
    ).executeQuery(
            new Object[]{ java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate), isGuest.flag },
            new Integer[]{ Types.DATE, Types.DATE, Types.INTEGER },
            rs -> {
              List<UsersObjectsCount> counts = new ArrayList<>();
              while (rs.next()) {
                int userCount = rs.getInt("USER_CNT");
                int objectsCount = rs.getInt("OBJECTS");
                UsersObjectsCount uac = new UsersObjectsCountImpl();
                uac.setUsersCount(userCount);
                uac.setObjectsCount(objectsCount);
                counts.add(uac);
              }
              return counts;
            }
    );
  }

  /***************************************************************************************
   *** Analysis object population methods
   **************************************************************************************/

  private static void populateSummaryFields(AnalysisSummary analysis, ResultSet rs) throws SQLException {
    analysis.setAnalysisId(rs.getString(COL_ANALYSIS_ID)); // varchar(50) not null,
    analysis.setStudyId(rs.getString(COL_STUDY_ID)); // varchar(50) not null,
    analysis.setStudyVersion(getStringOrEmpty(rs, COL_STUDY_VERSION)); // varchar(50),
    analysis.setApiVersion(getStringOrEmpty(rs, COL_API_VERSION)); // varchar(50),
    analysis.setDisplayName(rs.getString(COL_DISPLAY_NAME)); // varchar(50) not null,
    analysis.setDescription(getStringOrEmpty(rs, COL_DESCRIPTION)); // varchar(4000),
    analysis.setCreationTime(Utils.formatTimestamp(rs.getTimestamp(COL_CREATION_TIME))); // timestamp not null,
    analysis.setModificationTime(Utils.formatTimestamp(rs.getTimestamp(COL_MODIFICATION_TIME))); // timestamp not null,
    analysis.setIsPublic(Resources.getUserPlatform().getBooleanValue(rs, COL_IS_PUBLIC, false)); // integer not null,
    analysis.setNumFilters(rs.getInt(COL_NUM_FILTERS)); // integer not null,
    analysis.setNumComputations(rs.getInt(COL_NUM_COMPUTATIONS)); // integer not null,
    analysis.setNumVisualizations(rs.getInt(COL_NUM_VISUALIZATIONS)); // integer not null,
    analysis.setProvenance(createProvenance(Resources.getUserPlatform().getClobData(rs, COL_PROVENANCE))); // clob
  }

  private static AnalysisProvenance createProvenance(String onImportPropsString) {
    if (onImportPropsString == null) return null;
    AnalysisProvenance provenance = new AnalysisProvenanceImpl();
    provenance.setOnImport(Utils.parseObject(onImportPropsString, OnImportProvenanceProps.class));
    // current props will be assigned later
    return provenance;
  }

  private static String getStringOrEmpty(ResultSet rs, String colName) throws SQLException {
    return Optional.ofNullable(rs.getString(colName)).orElse("");
  }

  static void populateDetailFields(AnalysisDetailWithUser analysis, ResultSet rs) throws SQLException {
    analysis.setUserId(rs.getLong(COL_USER_ID));
    UserDataFactory.populateSummaryFields(analysis, rs);
    analysis.setDescriptor(Utils.parseObject(Resources.getUserPlatform().getClobData(rs, COL_DESCRIPTOR), AnalysisDescriptor.class)); // clob
    analysis.setNotes(Resources.getUserPlatform().getClobData(rs, COL_NOTES)); // clob
  }

  /***************************************************************************************
   *** Types and vals for analysis insert/update prepared statement vars
   **************************************************************************************/

  private static Object[] getAnalysisInsertValues(AnalysisDetailWithUser analysis) {
    return new Object[]{
        analysis.getAnalysisId(),
        analysis.getUserId(),
        analysis.getStudyId(),
        analysis.getStudyVersion(),
        analysis.getApiVersion(),
        analysis.getDisplayName(),
        analysis.getDescription(),
        Utils.parseDate(analysis.getCreationTime()),
        Utils.parseDate(analysis.getModificationTime()),
        Resources.getUserPlatform().convertBoolean(analysis.getIsPublic()),
        analysis.getNumFilters(),
        analysis.getNumComputations(),
        analysis.getNumVisualizations(),
        analysis.getProvenance() == null ? null : Utils.formatObject(analysis.getProvenance().getOnImport()),
        Utils.formatObject(analysis.getDescriptor()),
        analysis.getNotes()
    };
  }
}
