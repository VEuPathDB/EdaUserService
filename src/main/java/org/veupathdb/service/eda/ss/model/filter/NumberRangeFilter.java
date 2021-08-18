package org.veupathdb.service.eda.ss.model.filter;

import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.RdbmsColumnNames;

import static org.gusdb.fgputil.FormatUtil.NL;

<<<<<<< HEAD
public class NumberRangeFilter extends Filter {
=======
public class NumberRangeFilter extends SingleValueFilter {
>>>>>>> template/master

  private Number min;
  private Number max;
  
  public NumberRangeFilter(Entity entity, String variableId, Number min, Number max) {
    super(entity, variableId);
    this.min = min;
    this.max = max;
  }

  @Override
<<<<<<< HEAD
  public String getAndClausesSql() {
=======
  public String getFilteringAndClausesSql() {
>>>>>>> template/master
    return "  AND " + RdbmsColumnNames.NUMBER_VALUE_COL_NAME + " >= " + min + " AND " + RdbmsColumnNames.NUMBER_VALUE_COL_NAME + " <= " + max + NL;
  }

}
