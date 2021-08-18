package org.veupathdb.service.eda.ss.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.sql.DataSource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import org.veupathdb.service.eda.generated.model.APIDateRangeFilter;
import org.veupathdb.service.eda.generated.model.APIDateSetFilter;
import org.veupathdb.service.eda.generated.model.APIFilter;
import org.veupathdb.service.eda.generated.model.APILongitudeRangeFilter;
import org.veupathdb.service.eda.generated.model.APIMultiFilter;
import org.veupathdb.service.eda.generated.model.APIMultiFilterSubFilter;
import org.veupathdb.service.eda.generated.model.APINumberRangeFilter;
import org.veupathdb.service.eda.generated.model.APINumberSetFilter;
import org.veupathdb.service.eda.generated.model.APIStringSetFilter;
import org.veupathdb.service.eda.generated.model.APITabularReportConfig;
import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.MetadataCache;
import org.veupathdb.service.eda.ss.model.Study;
import org.veupathdb.service.eda.ss.model.TabularReportConfig;
import org.veupathdb.service.eda.ss.model.Variable;
import org.veupathdb.service.eda.ss.model.Variable.VariableDisplayType;
import org.veupathdb.service.eda.ss.model.filter.DateRangeFilter;
import org.veupathdb.service.eda.ss.model.filter.DateSetFilter;
import org.veupathdb.service.eda.ss.model.filter.Filter;
import org.veupathdb.service.eda.ss.model.filter.LongitudeRangeFilter;
import org.veupathdb.service.eda.ss.model.filter.MultiFilter;
import org.veupathdb.service.eda.ss.model.filter.MultiFilter.MultiFilterOperation;
import org.veupathdb.service.eda.ss.model.filter.MultiFilterSubFilter;
import org.veupathdb.service.eda.ss.model.filter.NumberRangeFilter;
import org.veupathdb.service.eda.ss.model.filter.NumberSetFilter;
import org.veupathdb.service.eda.ss.model.filter.StringSetFilter;

public class RequestBundle {

  static RequestBundle unpack(DataSource datasource, String studyId, String entityId, List<APIFilter> apiFilters, List<String> variableIds, APITabularReportConfig apiReportConfig) {
    String studIdStr = "Study ID " + studyId;
    if (!validateStudyId(datasource, studyId))
      throw new NotFoundException(studIdStr + " is not found.");

    Study study = MetadataCache.getStudy(studyId);
    Entity entity = study.getEntity(entityId).orElseThrow(() -> new NotFoundException("In " + studIdStr + " Entity ID not found: " + entityId));

    List<Variable> variables = getEntityVariables(entity, variableIds);

    List<Filter> filters = constructFiltersFromAPIFilters(study, apiFilters);
    
    TabularReportConfig reportConfig = constructTabularReportConfigFromAPIReportConfig(apiReportConfig);

    return new RequestBundle(study, entity, variables, filters, reportConfig);
  }

  /*
   * return true if valid study id
   */
  private static boolean validateStudyId(DataSource datasource, String studyId) {
    return MetadataCache.getStudyOverviews().stream()
            .anyMatch(study -> study.getId().equals(studyId));
  }

  private static List<Variable> getEntityVariables(Entity entity, List<String> variableIds) {

    List<Variable> variables = new ArrayList<>();

    for (String varId : variableIds) {
      String errMsg = "Variable '" + varId + "' is not found for entity with ID: '" + entity.getId() + "'";
      variables.add(entity.getVariable(varId).orElseThrow(() -> new BadRequestException(errMsg)));
    }
    return variables;
  }
  /*
   * Given a study and a set of API filters, construct and return a set of filters, each being the appropriate
   * filter subclass
   */
  static List<Filter> constructFiltersFromAPIFilters(Study study, List<APIFilter> filters) {
    List<Filter> subsetFilters = new ArrayList<>();

    String errPrfx = "A filter references an unfound ";

    for (APIFilter apiFilter : filters) {

      // validate filter's entity id
      Supplier<BadRequestException> excep =
          () -> new BadRequestException(errPrfx + "entity ID: " + apiFilter.getEntityId());
      Entity entity = study.getEntity(apiFilter.getEntityId()).orElseThrow(excep);

      Filter newFilter;
      if (apiFilter instanceof APIDateRangeFilter) {
        newFilter = unpackDateRangeFilter(apiFilter, entity);
      } else if (apiFilter instanceof APIDateSetFilter) {
        newFilter = unpackDateSetFilter(apiFilter, entity);
      } else if (apiFilter instanceof APINumberRangeFilter) {
        newFilter = unpackNumberRangeFilter(apiFilter, entity);
      } else if (apiFilter instanceof APINumberSetFilter) {
          newFilter = unpackNumberSetFilter(apiFilter, entity);
      } else if (apiFilter instanceof APILongitudeRangeFilter) {
        newFilter = unpackLongitudeRangeFilter(apiFilter, entity);
      } else if (apiFilter instanceof APIStringSetFilter) {
        newFilter = unpackStringSetFilter(apiFilter, entity);
      } else if (apiFilter instanceof APIMultiFilter) {
          newFilter = unpackMultiFilter(apiFilter, entity);
      } else
        throw new InternalServerErrorException("Input filter not an expected subclass of Filter");

      subsetFilters.add(newFilter);
    }
    return subsetFilters;
  }
  
  private static DateRangeFilter unpackDateRangeFilter(APIFilter apiFilter, Entity entity) {
      APIDateRangeFilter f = (APIDateRangeFilter)apiFilter;
      String varId = f.getVariableId();
      entity.getVariable(varId).orElseThrow(() -> new BadRequestException("Variable '" + varId + "' is not found"));
      return new DateRangeFilter(entity, varId, parseDate(f.getMin()), parseDate(f.getMax()));
  }
  
  private static DateSetFilter unpackDateSetFilter(APIFilter apiFilter, Entity entity) {
      APIDateSetFilter f = (APIDateSetFilter)apiFilter;
      String varId = f.getVariableId();
      entity.getVariable(varId).orElseThrow(() -> new BadRequestException("Variable '" + varId + "' is not found"));
      List<LocalDateTime> dateSet = new ArrayList<>();
      for (String dateStr : f.getDateSet()) dateSet.add(parseDate(dateStr));
      return new DateSetFilter(entity, varId, dateSet);
  }
  
  private static NumberRangeFilter unpackNumberRangeFilter(APIFilter apiFilter, Entity entity) {
      APINumberRangeFilter f = (APINumberRangeFilter)apiFilter;
      String varId = f.getVariableId();
      entity.getVariable(varId).orElseThrow(() -> new BadRequestException("Variable '" + varId + "' is not found"));
      return new NumberRangeFilter(entity, varId, f.getMin(), f.getMax());
  }
  
  private static NumberSetFilter unpackNumberSetFilter(APIFilter apiFilter, Entity entity) {
      APINumberSetFilter f = (APINumberSetFilter)apiFilter;
      String varId = f.getVariableId();
      entity.getVariable(varId).orElseThrow(() -> new BadRequestException("Variable '" + varId + "' is not found"));
      return new NumberSetFilter(entity, varId, f.getNumberSet());
  }
  
  private static LongitudeRangeFilter unpackLongitudeRangeFilter(APIFilter apiFilter, Entity entity) {
      APILongitudeRangeFilter f = (APILongitudeRangeFilter)apiFilter;
      String varId = f.getVariableId();
      entity.getVariable(varId).orElseThrow(() -> new BadRequestException("Variable '" + varId + "' is not found"));
      return new LongitudeRangeFilter(entity, varId, f.getLeft(), f.getRight());
  }
  
  private static StringSetFilter unpackStringSetFilter(APIFilter apiFilter, Entity entity) {
      APIStringSetFilter f = (APIStringSetFilter)apiFilter;
      String varId = f.getVariableId();
      entity.getVariable(varId).orElseThrow(() -> new BadRequestException("Variable '" + varId + "' is not found"));
      return new StringSetFilter(entity, varId, f.getStringSet());
  }
  
  private static MultiFilter unpackMultiFilter(APIFilter apiFilter, Entity entity) {
      APIMultiFilter f = (APIMultiFilter)apiFilter;
      if (f.getSubFilters().isEmpty()) 
    	  throw new BadRequestException("Multifilter may not have an empty list of subFilters");
      String multiFilterParentId = getMultiFilterParentVariableId(entity, f);
      
      List<MultiFilterSubFilter> subFilters = new ArrayList<>();
      for (APIMultiFilterSubFilter apiSubFilter : f.getSubFilters()) {
    	  String variableId = apiSubFilter.getVariableId();
    	  Variable var = entity.getVariable(variableId).orElseThrow(() -> new BadRequestException("Multifilter includes invalid variable ID: " + variableId));
    	  if (!var.getParentId().equals(multiFilterParentId))
    		  throw new BadRequestException("Multifilter includes variable with invalid parent.  Variable: " + variableId);
    	  subFilters.add(new MultiFilterSubFilter(var, apiSubFilter.getStringSet()));
      }
      return new MultiFilter(entity, subFilters,  MultiFilterOperation.fromString(f.getOperation().getValue()));
  }
  
  private static String getMultiFilterParentVariableId(Entity entity, APIMultiFilter apiFilter) {
	  String firstVariableId = apiFilter.getSubFilters().get(0).getVariableId();
	  Variable firstVariable = entity.getVariable(firstVariableId).orElseThrow(() -> 
	    new BadRequestException("Multifilter includes invalid variable ID: " + firstVariableId));
	  String parentId = firstVariable.getParentId();
	  if (parentId == null)
		  throw new BadRequestException("Multifilter includes variable with null parent ID: " + firstVariableId);
	  Variable parentVariable = entity.getVariable(parentId) .orElseThrow(() -> 
	    new BadRequestException("Multifilter includes invalid parent variable ID: " + parentId));
	  if (parentVariable.getDisplayType() != VariableDisplayType.MULTIFILTER)
		  throw new BadRequestException("Multifilter parent variable does not have display type 'multifilter': " + parentId);

	  return parentId;
  }
  
  static TabularReportConfig constructTabularReportConfigFromAPIReportConfig(APITabularReportConfig apiConfig) {
	  if (apiConfig == null) return null;
	  Integer numRows = null;
	  Integer offset = null;
	  if (apiConfig.getPagingConfig() != null) {
		  numRows = apiConfig.getPagingConfig().getNumRows();
		  offset = apiConfig.getPagingConfig().getOffset();
	  }
	  return new TabularReportConfig(apiConfig.getSortingColumns(), numRows,
			  offset);		  
  }

  public static LocalDateTime parseDate(String dateStr) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
      return LocalDateTime.parse(dateStr, formatter);
    }
    catch (DateTimeParseException e) {
      throw new BadRequestException("Can't parse date/time string: " + dateStr);
    }
  }

  public static String formatDate(LocalDateTime dateTime) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
      return formatter.format(dateTime);
    }
    catch (DateTimeParseException e) {
      throw new RuntimeException("Can't format date: " + dateTime);
    }
  }
  private final Study _study;
  private final List<Filter> _filters;
  private final Entity _targetEntity;
  private final List<Variable> _requestedVariables;
  private final TabularReportConfig _reportConfig;

  RequestBundle(Study study, Entity targetEntity, List<Variable> requestedVariables, List<Filter> filters, TabularReportConfig reportConfig) {
    _study = study;
    _targetEntity = targetEntity;
    _filters = filters;
    _requestedVariables = requestedVariables;
    _reportConfig = reportConfig;
  }

  public Study getStudy() {
    return _study;
  }

  public List<Filter> getFilters() {
    return _filters;
  }

  public Entity getTargetEntity() {
    return _targetEntity;
  }

  public List<Variable> getRequestedVariables() {
    return _requestedVariables;
  }
  
  public TabularReportConfig getReportConfig() {
	  return _reportConfig;
  }
}
