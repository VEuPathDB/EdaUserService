package org.veupathdb.service.eda.ss.model.filter;

import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.RdbmsColumnNames;

import static org.gusdb.fgputil.FormatUtil.NL;

public class NumberRangeFilter extends Filter {

  private Number min;
  private Number max;
  
  public NumberRangeFilter(Entity entity, String variableId, Number min, Number max) {
    super(entity, variableId);
    this.min = min;
    this.max = max;
  }

  @Override
  public String getAndClausesSql() {
    return "  AND " + RdbmsColumnNames.NUMBER_VALUE_COL_NAME + " >= " + min + " AND " + RdbmsColumnNames.NUMBER_VALUE_COL_NAME + " <= " + max + NL;
  }

}
