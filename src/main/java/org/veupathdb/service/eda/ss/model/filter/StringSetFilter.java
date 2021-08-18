package org.veupathdb.service.eda.ss.model.filter;

import java.util.List;
import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.RdbmsColumnNames;

import static org.gusdb.fgputil.FormatUtil.NL;


<<<<<<< HEAD
public class StringSetFilter extends Filter {
=======
public class StringSetFilter extends SingleValueFilter {
>>>>>>> template/master

  private List<String> stringSet;
  
  public StringSetFilter(Entity entity, String variableId, List<String> stringSet) {
    super(entity, variableId);
    this.stringSet = stringSet;
  }

  @Override
<<<<<<< HEAD
  public String getAndClausesSql() {
=======
  public String getFilteringAndClausesSql() {
>>>>>>> template/master
    return "  AND " + RdbmsColumnNames.STRING_VALUE_COL_NAME + " IN ('" + String.join("', '", stringSet) + "')" + NL;
  }

}
