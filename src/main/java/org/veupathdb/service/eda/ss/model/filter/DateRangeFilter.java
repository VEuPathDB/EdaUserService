package org.veupathdb.service.eda.ss.model.filter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.veupathdb.service.eda.ss.model.Entity;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.veupathdb.service.eda.ss.model.RdbmsColumnNames.DATE_VALUE_COL_NAME;

public class DateRangeFilter extends Filter {

  private LocalDateTime min;
  private LocalDateTime max;
  
  public DateRangeFilter(Entity entity, String variableId, LocalDateTime min, LocalDateTime max) {
    super(entity, variableId);
    this.min = min;
    this.max = max;
  }

  @Override
  public String getAndClausesSql() {
    return "  AND " + DATE_VALUE_COL_NAME + " >= " + dbDateTimeIsoValue(min) + " AND " + DATE_VALUE_COL_NAME + " <= " + dbDateTimeIsoValue(max) + NL;
  }

  static String dbDateTimeIsoValue(LocalDateTime dateTime) {
    String str = dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    return "TO_DATE('" + str + "', 'YYYY-MM-DD\"T\"HH24:MI:SS')";
  }
}
