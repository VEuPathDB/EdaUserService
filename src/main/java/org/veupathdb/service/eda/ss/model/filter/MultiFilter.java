package org.veupathdb.service.eda.ss.model.filter;

import java.util.ArrayList;
import java.util.List;

import static org.gusdb.fgputil.FormatUtil.NL;
import org.veupathdb.service.eda.ss.model.Entity;

public class MultiFilter extends Filter {
	
	private final List<MultiFilterSubFilter> subFilters;
	private final MultiFilterOperation operation;
	
	public enum MultiFilterOperation {
		UNION("UNION"), INTERSECT("INTERSECT");

		private final String operation;

		MultiFilterOperation(String operation) {
			this.operation = operation;
		}
		
		String getOperation() { return operation; }
		
	    public static MultiFilterOperation fromString(String operation) {

	        return switch (operation) {
	          case "intersect" -> INTERSECT;
	          case "union" -> UNION;
	          default -> throw new RuntimeException("Unrecognized multi-filter operation: " + operation);
	        
	      };
	    }   
	}
	
	@Override
	public String getSql() {
		List<String> subFiltersSqlList = new ArrayList<String>();
		for (MultiFilterSubFilter subFilter : subFilters) subFiltersSqlList.add(getSingleFilterSql(subFilter));
		String subFiltersSql = String.join("  " + operation.getOperation() + NL, subFiltersSqlList);
		return "  select * from ( -- START OF MULTIFILTER" + NL
				+ subFiltersSql + NL
				+ "  ) -- END OF MULTIFILTER" + NL;
	}

	public MultiFilter(Entity entity, List<MultiFilterSubFilter> subFilters, MultiFilterOperation operation) {
		super(entity);
		this.subFilters = subFilters;
		this.operation = operation;
	}
	
	private String getSingleFilterSql(MultiFilterSubFilter subFilter) {
		StringSetFilter ssf = new StringSetFilter(entity, subFilter.getVariable().getId(), subFilter.getStringSet());
		return ssf.getSql();
	}

}
