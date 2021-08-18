package org.veupathdb.service.eda.ss.model.filter;

<<<<<<< HEAD
import org.veupathdb.service.eda.ss.Resources;
import org.veupathdb.service.eda.ss.model.Entity;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.veupathdb.service.eda.ss.model.RdbmsColumnNames.*;

public abstract class Filter {
  protected Entity entity;
  protected String variableId;
  
  public Filter(Entity entity, String variableId) {
    if (entity == null) throw new RuntimeException("Null entity not allowed");
    entity.getVariable(variableId).orElseThrow(() -> new RuntimeException("Entity " + entity.getId() + " does not contain variable " + variableId));
    this.entity = entity;
    this.variableId = variableId;
  }

  public String getSql() {
    return entity.getAncestorPkColNames().isEmpty()?
        getSqlNoAncestors() :
          getSqlWithAncestors();
  }

  private String getSqlWithAncestors() {

    return "  SELECT " + entity.getAllPksSelectList("t", "a") + NL
        + "  FROM " + Resources.getAppDbSchema() + entity.getTallTableName() + " t, " + Resources.getAppDbSchema() + entity.getAncestorsTableName() + " a" + NL
        + "  WHERE t." + entity.getPKColName() + " = a." + entity.getPKColName() + NL
        + "  AND " + TT_VARIABLE_ID_COL_NAME + " = '" + variableId + "'" + NL
        + getAndClausesSql();
  }
  
  private String getSqlNoAncestors() {
    
    return "  SELECT " + entity.getPKColName() + NL
        + "  FROM " + Resources.getAppDbSchema() + entity.getTallTableName() + NL
        + "  WHERE " + TT_VARIABLE_ID_COL_NAME + " = '" + variableId + "'" + NL
        + getAndClausesSql();
  }

  /**
   * subclasses provide AND clauses specific to their type
   */
  public abstract String getAndClausesSql();
=======
import static org.gusdb.fgputil.FormatUtil.NL;
import org.veupathdb.service.eda.ss.Resources;
import org.veupathdb.service.eda.ss.model.Entity;

public abstract class Filter {
  protected Entity entity;
  
  public Filter(Entity entity) {
    if (entity == null) throw new RuntimeException("Null entity not allowed");
    this.entity = entity;
  }

  public abstract String getSql();

	/*
	 * Get SQL to perform the filter. Include ancestor IDs.
	 */
     protected String getSingleFilterCommonSqlWithAncestors() {
  
	// join to ancestors table to get ancestor ID

	    return "  SELECT " + entity.getAllPksSelectList("a") + NL
	        + "  FROM " + Resources.getAppDbSchema() + entity.getTallTableName() + " t, " + Resources.getAppDbSchema() + entity.getAncestorsTableName() + " a" + NL
	        + "  WHERE t." + entity.getPKColName() + " = a." + entity.getPKColName();
	}

	protected String getSingleFilterCommonSqlNoAncestors() {

		return "  SELECT " + entity.getPKColName() + NL
				+ "  FROM " + Resources.getAppDbSchema() + entity.getTallTableName() + NL
				+ "  WHERE 1 = 1 --no-op where clause for code generation simplicity";
	}
>>>>>>> template/master

  public Entity getEntity() {
    return entity;
  }
  
  
}
