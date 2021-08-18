package org.veupathdb.service.eda.ss.model;

import java.util.List;

public class TabularReportConfig {
	  private List<String> sortingColumns;
	  private Integer numRows;
	  private Integer offset;
	  
	public TabularReportConfig(List<String> sortingColumns, Integer numRows, Integer offset) {
		this.sortingColumns = sortingColumns;
		this.numRows = numRows;
		this.offset = offset;
	}

	public List<String> getSortingColumns() {
		return sortingColumns;
	}

	public Integer getNumRows() {
		return numRows;
	}

	public Integer getOffset() {
		return offset;
	}
	  
}
