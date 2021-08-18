package org.veupathdb.service.eda.ss.model;

import java.sql.Date;
import java.sql.ResultSet;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.FunctionalInterfaces;
import org.gusdb.fgputil.functional.Functions;

public enum VariableType {
  STRING("string_value", rs -> rs.getString("string_value"), "string"),
  NUMBER("number_value", rs -> doubleValueOrNull(rs, rs.getDouble("number_value")), "number"),
  DATE("date_value", rs -> dateValueOrNull(rs.getDate("date_value")), "date"),
  LONGITUDE("number_value", rs -> doubleValueOrNull(rs, rs.getDouble("number_value")), "longitude");

  private final String tallTableColumnName;
  private final String typeString;
  private final FunctionalInterfaces.FunctionWithException<ResultSet, String> resultSetToStringValue;

  VariableType(String tallTableColumnName, FunctionalInterfaces.FunctionWithException<ResultSet, String> resultSetToStringValue, String typeString) {
    this.tallTableColumnName = tallTableColumnName;
    this.resultSetToStringValue = resultSetToStringValue;
    this.typeString = typeString;
  }

  public String getTallTableColumnName() {
    return this.tallTableColumnName;
  }

  public static VariableType fromString(String str) {
    if (str.equals(STRING.typeString) || str.equals("boolean")) return STRING;  // TODO remove boolean hack
    else if (str.equals(NUMBER.typeString)) return NUMBER;
    else if (str.equals(LONGITUDE.typeString)) return LONGITUDE;
    else if (str.equals(DATE.typeString)) return DATE;
    else throw new RuntimeException("Illegal variable type string: " + str);
  }

  public String convertRowValueToStringValue(ResultSet rs) {
    try {
      return resultSetToStringValue.apply(rs);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // utility to convert null DB double values to real null
  private static String doubleValueOrNull(ResultSet rs, double value) {
    return Functions.swallowAndGet(() -> rs.wasNull() ? null : String.valueOf(value));
  }

  // utility to convert null DB date values to real null
  private static String dateValueOrNull(Date value) {
    return value == null ? null : FormatUtil.formatDateTimeNoTimezone(value);
  }
}
