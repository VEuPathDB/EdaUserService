package org.veupathdb.service.eda.us.model;

import java.sql.Types;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.service.eda.us.Resources;
import org.veupathdb.service.eda.us.Utils;

public class UserDataFactory {

  private static final String SCHEMA_MACRO = "SCHEMA";

  private static final String INSERT_USER_SQL =
      "insert into " + SCHEMA_MACRO + "users" +
      " select ? as user_id, ? as is_guest, '{}' as preferences from dual" +
      " where not exists (select user_id from " + SCHEMA_MACRO + "users);";

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
    DBPlatform platform = Resources.getUserPlatform();
    new SQLRunner(
        Resources.getUserDataSource(),
        addSchema(INSERT_USER_SQL),
        "insert-user"
    ).executeStatement(
        new Object[]{ user.getUserID(), platform.convertBoolean(user.isGuest()) },
        new Integer[]{ Types.BIGINT, platform.getBooleanType() }
    );
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
