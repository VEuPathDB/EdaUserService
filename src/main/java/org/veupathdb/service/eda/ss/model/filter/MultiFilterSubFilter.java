package org.veupathdb.service.eda.ss.model.filter;

import java.util.List;

import org.veupathdb.service.eda.ss.model.Variable;

public class MultiFilterSubFilter {
	private final Variable variable;
	private final List<String> stringSet;
	public MultiFilterSubFilter(Variable variable, List<String> stringSet) {
		super();
		this.variable = variable;
		this.stringSet = stringSet;
	}
	public Variable getVariable() {
		return variable;
	}
	public List<String> getStringSet() {
		return stringSet;
	}
}
