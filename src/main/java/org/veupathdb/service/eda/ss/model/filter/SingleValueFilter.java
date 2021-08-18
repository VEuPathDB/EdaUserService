package org.veupathdb.service.eda.ss.model.filter;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.veupathdb.service.eda.ss.model.RdbmsColumnNames.TT_VARIABLE_ID_COL_NAME;

import org.veupathdb.service.eda.ss.model.Entity;

public abstract class SingleValueFilter extends Filter {

	protected String variableId;

	public SingleValueFilter(Entity entity, String variableId) {
		super(entity);
		entity.getVariable(variableId).orElseThrow(
				() -> new RuntimeException("Entity " + entity.getId() + " does not contain variable " + variableId));
		this.variableId = variableId;
	}

	@Override
	public String getSql() {
		return entity.getAncestorPkColNames().isEmpty() ? getSqlNoAncestors() : getSqlWithAncestors();
	}

	/**
	 * subclasses provide an AND clause to find rows that match their filter
	 */
	public abstract String getFilteringAndClausesSql();	  

	// join to ancestors table to get ancestor IDs
	String getSqlWithAncestors() {

	    return getSingleFilterCommonSqlWithAncestors() + NL
		    + "  AND " + TT_VARIABLE_ID_COL_NAME + " = '" + variableId + "'" + NL
	        + getFilteringAndClausesSql();
	}
	  
	String getSqlNoAncestors() {
	    
	    return getSingleFilterCommonSqlWithAncestors() + NL
	        + "  AND " + TT_VARIABLE_ID_COL_NAME + " = '" + variableId + "'" + NL
	        + getFilteringAndClausesSql();
	}

}
