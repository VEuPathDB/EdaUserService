package org.veupathdb.service.eda.us.model;

import java.sql.Types;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.veupathdb.lib.container.jaxrs.model.User;
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

  private static String addSchema(String sqlConstant) {
    return sqlConstant.replace(SCHEMA_MACRO, Resources.getUserDbSchema());
  }

  private static void addUserIfAbsent(User user) {
    String sql = String.format(
        addSchema(INSERT_USER_SQL),
        user.getUserID(),
        Resources.getUserPlatform().convertBoolean(user.isGuest()),
        user.getUserID());
    LOG.info("Trying to insert user with SQL: " + sql);
    int newRows = new SQLRunner(Resources.getUserDataSource(), sql, "insert-user").executeUpdate();
    LOG.info(newRows == 0 ? "User with ID " + user.getUserID() + " already present." : "New user inserted.");
  }

  public static String readPreferences(User user) {
    addUserIfAbsent(user);
    return new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(READ_PREFS_SQL),
        "read-prefs"
    ).executeQuery(
        new Object[]{ user.getUserID() },
        new Integer[]{ Types.BIGINT },
        rs -> rs.next()
          ? Resources.getUserPlatform().getClobData(rs, "preferences")
          : Utils.doThrow(new IllegalStateException("User unexpectedly not found"))
    );
  }

  public static void writePreferences(User user, String prefsObject) {
    addUserIfAbsent(user);
    new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(WRITE_PREFS_SQL),
        "write-prefs"
    ).executeStatement(
        new Object[]{ prefsObject, user.getUserID() },
        new Integer[]{ Types.CLOB, Types.BIGINT }
    );
  }


}
