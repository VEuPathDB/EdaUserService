package org.veupathdb.service.eda.ss.model.filter;

import java.util.List;
import java.util.stream.Collectors;
import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.RdbmsColumnNames;

import static org.gusdb.fgputil.FormatUtil.NL;

public class NumberSetFilter extends Filter {

  private List<Number> numberSet;
  
  public NumberSetFilter(Entity entity, String variableId, List<Number> numberSet) {
    super(entity, variableId);
    this.numberSet = numberSet;
  }

  @Override
  public String getAndClausesSql() {
    List<String> vals = numberSet.stream().map(String::valueOf).collect(Collectors.toList());
    return "  AND " + RdbmsColumnNames.NUMBER_VALUE_COL_NAME + " IN (" + String.join(", ", vals) + " )" + NL;
  }

}
