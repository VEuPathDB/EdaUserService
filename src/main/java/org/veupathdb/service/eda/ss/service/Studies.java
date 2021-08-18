package org.veupathdb.service.eda.ss.service;

<<<<<<< HEAD
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.sql.DataSource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.TreeNode;
import org.veupathdb.service.eda.generated.model.APIDateRangeFilter;
import org.veupathdb.service.eda.generated.model.APIDateSetFilter;
=======
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.json.JsonUtil;
>>>>>>> template/master
import org.veupathdb.service.eda.generated.model.APIDateVariable;
import org.veupathdb.service.eda.generated.model.APIDateVariableImpl;
import org.veupathdb.service.eda.generated.model.APIEntity;
import org.veupathdb.service.eda.generated.model.APIEntityImpl;
<<<<<<< HEAD
import org.veupathdb.service.eda.generated.model.APIFilter;
import org.veupathdb.service.eda.generated.model.APILongitudeRangeFilter;
import org.veupathdb.service.eda.generated.model.APILongitudeVariable;
import org.veupathdb.service.eda.generated.model.APILongitudeVariableImpl;
import org.veupathdb.service.eda.generated.model.APINumberRangeFilter;
import org.veupathdb.service.eda.generated.model.APINumberSetFilter;
import org.veupathdb.service.eda.generated.model.APINumberVariable;
import org.veupathdb.service.eda.generated.model.APINumberVariableImpl;
import org.veupathdb.service.eda.generated.model.APIStringSetFilter;
=======
import org.veupathdb.service.eda.generated.model.APILongitudeVariable;
import org.veupathdb.service.eda.generated.model.APILongitudeVariableImpl;
import org.veupathdb.service.eda.generated.model.APINumberVariable;
import org.veupathdb.service.eda.generated.model.APINumberVariableImpl;
>>>>>>> template/master
import org.veupathdb.service.eda.generated.model.APIStringVariable;
import org.veupathdb.service.eda.generated.model.APIStringVariableImpl;
import org.veupathdb.service.eda.generated.model.APIStudyDetail;
import org.veupathdb.service.eda.generated.model.APIStudyDetailImpl;
<<<<<<< HEAD
import org.veupathdb.service.eda.generated.model.APIStudyOverview;
import org.veupathdb.service.eda.generated.model.APIStudyOverviewImpl;
=======
>>>>>>> template/master
import org.veupathdb.service.eda.generated.model.APIVariable;
import org.veupathdb.service.eda.generated.model.APIVariableDataShape;
import org.veupathdb.service.eda.generated.model.APIVariableDisplayType;
import org.veupathdb.service.eda.generated.model.APIVariablesCategory;
import org.veupathdb.service.eda.generated.model.APIVariablesCategoryImpl;
<<<<<<< HEAD
=======
import org.veupathdb.service.eda.generated.model.BinSpecWithRange;
import org.veupathdb.service.eda.generated.model.BinSpecWithRangeImpl;
>>>>>>> template/master
import org.veupathdb.service.eda.generated.model.EntityCountPostRequest;
import org.veupathdb.service.eda.generated.model.EntityCountPostResponse;
import org.veupathdb.service.eda.generated.model.EntityCountPostResponseImpl;
import org.veupathdb.service.eda.generated.model.EntityIdGetResponse;
import org.veupathdb.service.eda.generated.model.EntityIdGetResponseImpl;
import org.veupathdb.service.eda.generated.model.EntityTabularPostRequest;
import org.veupathdb.service.eda.generated.model.EntityTabularPostResponseStream;
import org.veupathdb.service.eda.generated.model.StudiesGetResponseImpl;
import org.veupathdb.service.eda.generated.model.StudyIdGetResponse;
import org.veupathdb.service.eda.generated.model.StudyIdGetResponseImpl;
<<<<<<< HEAD
import org.veupathdb.service.eda.generated.model.VariableDistributionPostRequest;
import org.veupathdb.service.eda.generated.model.VariableDistributionPostResponseStream;
import org.veupathdb.service.eda.ss.Resources;
import org.veupathdb.service.eda.ss.model.filter.DateRangeFilter;
import org.veupathdb.service.eda.ss.model.filter.DateSetFilter;
import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.filter.Filter;
import org.veupathdb.service.eda.ss.model.filter.LongitudeRangeFilter;
import org.veupathdb.service.eda.ss.model.filter.NumberRangeFilter;
import org.veupathdb.service.eda.ss.model.filter.NumberSetFilter;
import org.veupathdb.service.eda.ss.model.filter.StringSetFilter;
import org.veupathdb.service.eda.ss.model.Study;
import org.veupathdb.service.eda.ss.model.StudySubsettingUtils;
import org.veupathdb.service.eda.ss.model.Variable;
import org.veupathdb.service.eda.ss.model.Variable.VariableType;

import static org.gusdb.fgputil.functional.Functions.cSwallow;

public class Studies implements org.veupathdb.service.eda.generated.resources.Studies {

  static Map<String, APIStudyOverview> apiStudyOverviews;  // cache the overviews
  static Map<String, Study> studies = new ConcurrentHashMap<>(); // cache the studies

  private Study getStudy(String studyId){
    return studies.computeIfAbsent(studyId, id ->
        Study.loadStudy(Resources.getApplicationDataSource(), id));
=======
import org.veupathdb.service.eda.generated.model.ValueSpec;
import org.veupathdb.service.eda.generated.model.VariableDistributionPostRequest;
import org.veupathdb.service.eda.generated.model.VariableDistributionPostResponse;
import org.veupathdb.service.eda.generated.model.VariableDistributionPostResponseImpl;
import org.veupathdb.service.eda.ss.Resources;
import org.veupathdb.service.eda.ss.model.DateVariable;
import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.MetadataCache;
import org.veupathdb.service.eda.ss.model.NumberVariable;
import org.veupathdb.service.eda.ss.model.StringVariable;
import org.veupathdb.service.eda.ss.model.Study;
import org.veupathdb.service.eda.ss.model.StudySubsettingUtils;
import org.veupathdb.service.eda.ss.model.Variable;
import org.veupathdb.service.eda.ss.model.VariableType;
import org.veupathdb.service.eda.ss.model.VariableWithValues;
import org.veupathdb.service.eda.ss.model.distribution.AbstractDistribution;
import org.veupathdb.service.eda.ss.model.distribution.DateBinDistribution;
import org.veupathdb.service.eda.ss.model.distribution.DiscreteDistribution;
import org.veupathdb.service.eda.ss.model.distribution.DistributionResult;
import org.veupathdb.service.eda.ss.model.distribution.NumberBinDistribution;
import org.veupathdb.service.eda.ss.model.filter.Filter;

public class Studies implements org.veupathdb.service.eda.generated.resources.Studies {

  private static final Logger LOG = LogManager.getLogger(Studies.class);

  @Override
  public GetStudiesClearMetadataCacheResponse getStudiesClearMetadataCache() {
    MetadataCache.clear();
    return GetStudiesClearMetadataCacheResponse.respond202();
>>>>>>> template/master
  }

  @Override
  public GetStudiesResponse getStudies() {
    var out = new StudiesGetResponseImpl();
<<<<<<< HEAD
    out.setStudies(getStudyOverviews(Resources.getApplicationDataSource()));
    return GetStudiesResponse.respond200WithApplicationJson(out);
  }

  private List<APIStudyOverview> getStudyOverviews(DataSource datasource) {
    if (apiStudyOverviews == null) {
      List<Study.StudyOverview> overviews = Study.getStudyOverviews(datasource);
      Map<String, APIStudyOverview> tmp = new LinkedHashMap<>();
      for (Study.StudyOverview overview : overviews) {
        APIStudyOverview study = new APIStudyOverviewImpl();
        study.setId(overview.getId());
        study.setDatasetId(overview.getDatasetId());
        tmp.put(study.getId(), study);
      }
      apiStudyOverviews = tmp;
    }
    return new ArrayList<>( apiStudyOverviews.values() );
  }

  @Override
  public GetStudiesByStudyIdResponse getStudiesByStudyId(String studyId) {
    Study study = getStudy(studyId);
    APIStudyDetail apiStudyDetail = getApiStudyDetail(study);
    StudyIdGetResponse response = new StudyIdGetResponseImpl();
    response.setStudy(apiStudyDetail);
    
=======
    out.setStudies(MetadataCache.getStudyOverviews());
    return GetStudiesResponse.respond200WithApplicationJson(out);
  }

  @Override
  public GetStudiesByStudyIdResponse getStudiesByStudyId(String studyId) {
    Study study = MetadataCache.getStudy(studyId);
    APIStudyDetail apiStudyDetail = getApiStudyDetail(study);
    StudyIdGetResponse response = new StudyIdGetResponseImpl();
    response.setStudy(apiStudyDetail);
>>>>>>> template/master
    return GetStudiesByStudyIdResponse.respond200WithApplicationJson(response);
  }

  @Override
  public GetStudiesEntitiesByStudyIdAndEntityIdResponse getStudiesEntitiesByStudyIdAndEntityId(String studyId, String entityId) {
<<<<<<< HEAD
    APIStudyDetail apiStudyDetail = getApiStudyDetail(getStudy(studyId));
=======
    APIStudyDetail apiStudyDetail = getApiStudyDetail(MetadataCache.getStudy(studyId));
>>>>>>> template/master
    APIEntity entity = findEntityById(apiStudyDetail.getRootEntity(), entityId).orElseThrow(NotFoundException::new);
    EntityIdGetResponse response = new EntityIdGetResponseImpl();
    // copy properties of found entity, skipping children
    response.setId(entity.getId());
    response.setDescription(entity.getDescription());
    response.setDisplayName(entity.getDisplayName());
    response.setDisplayNamePlural(entity.getDisplayNamePlural());
    response.setVariables(entity.getVariables());
    for (Map.Entry<String,Object> prop : entity.getAdditionalProperties().entrySet()) {
      response.setAdditionalProperties(prop.getKey(), prop.getValue());
    }
    return GetStudiesEntitiesByStudyIdAndEntityIdResponse.respond200WithApplicationJson(response);
  }

<<<<<<< HEAD
  private  Optional<APIEntity> findEntityById(APIEntity entity, String entityId) {
=======
  private Optional<APIEntity> findEntityById(APIEntity entity, String entityId) {
>>>>>>> template/master
    if (entity.getId().equals(entityId)) {
      return Optional.of(entity);
    }
    for (APIEntity child : entity.getChildren()){
      Optional<APIEntity> foundEntity = findEntityById(child, entityId);
      if (foundEntity.isPresent()) return foundEntity;
    }
    return Optional.empty();
  }

  public static APIStudyDetail getApiStudyDetail(Study study) {
<<<<<<< HEAD

    APIEntity apiEntityTree = entityTreeToAPITree(study.getEntityTree());

=======
    APIEntity apiEntityTree = entityTreeToAPITree(study.getEntityTree());
>>>>>>> template/master
    APIStudyDetail apiStudyDetail = new APIStudyDetailImpl();
    apiStudyDetail.setId(study.getStudyId());
    apiStudyDetail.setDatasetId(study.getDatasetId());
    apiStudyDetail.setRootEntity(apiEntityTree);
<<<<<<< HEAD
    // TODO: lose or fill in study.setName() prop
=======
>>>>>>> template/master
    return apiStudyDetail;
  }
  
  public static APIEntity entityTreeToAPITree(TreeNode<Entity> root) {
    return root.mapStructure((entity, mappedChildren) -> {
      APIEntity apiEntity = new APIEntityImpl();
      apiEntity.setDescription(entity.getDescription());
      apiEntity.setDisplayName(entity.getDisplayName());
<<<<<<< HEAD
      apiEntity.setId(entity.getId());
      apiEntity.setIdColumnName(entity.getPKColName());
      apiEntity.setChildren(mappedChildren);
      
      List<APIVariable> apiVariables = new ArrayList<>();
      for (Variable var : entity.getVariables())
        apiVariables.add(variableToAPIVariable(var));
      apiEntity.setVariables(apiVariables);

=======
      apiEntity.setDisplayNamePlural(entity.getDisplayNamePlural());
      apiEntity.setId(entity.getId());
      apiEntity.setIdColumnName(entity.getPKColName());
      apiEntity.setChildren(mappedChildren);
      apiEntity.setVariables(entity.getVariables().stream()
          .map(var -> variableToAPIVariable(var))
          .collect(Collectors.toList()));
>>>>>>> template/master
      return apiEntity;
    });
  }
  
  private static APIVariable variableToAPIVariable(Variable var) {
    if (!var.getHasValues()) {
      APIVariablesCategory apiVar = new APIVariablesCategoryImpl();
<<<<<<< HEAD
      setApiVarProps(apiVar, var);
      return apiVar;
    }
    if (var.getType() == VariableType.DATE) {
      APIDateVariable apiVar = new APIDateVariableImpl();
      setApiVarProps(apiVar, var);
      apiVar.setDataShape(APIVariableDataShape.valueOf(var.getDataShape().toString()));
      apiVar.setDisplayType(APIVariableDisplayType.valueOf(var.getDisplayType().toString()));
      return apiVar;
    }
    else if (var.getType() == VariableType.NUMBER) {
      APINumberVariable apiVar = new APINumberVariableImpl();
      setApiVarProps(apiVar, var);
      apiVar.setUnits(var.getUnits());
      apiVar.setDataShape(APIVariableDataShape.valueOf(var.getDataShape().toString()));
      apiVar.setDisplayType(APIVariableDisplayType.valueOf(var.getDisplayType().toString()));
      return apiVar;
    }
    else if (var.getType() == VariableType.STRING) {
      APIStringVariable apiVar = new APIStringVariableImpl();
      apiVar.setDataShape(APIVariableDataShape.valueOf(var.getDataShape().toString()));
      apiVar.setDisplayType(APIVariableDisplayType.valueOf(var.getDisplayType().toString()));
      setApiVarProps(apiVar, var);
      return apiVar;
    }
    else if (var.getType() == VariableType.LONGITUDE) {
      APILongitudeVariable apiVar = new APILongitudeVariableImpl();
      apiVar.setDataShape(APIVariableDataShape.valueOf(var.getDataShape().toString()));
      apiVar.setDisplayType(APIVariableDisplayType.valueOf(var.getDisplayType().toString()));
      setApiVarProps(apiVar, var);
=======
      apiVar.setId(var.getId());
      apiVar.setDisplayName(var.getDisplayName());
      apiVar.setProviderLabel(var.getProviderLabel());
      apiVar.setParentId(var.getParentId());
      apiVar.setDefinition(var.getDefinition());
      apiVar.setDisplayOrder(var.getDisplayOrder());
      return apiVar;
    }
    if (var.getType() == VariableType.DATE) {
      DateVariable dateVar = (DateVariable)var;
      APIDateVariable apiVar = new APIDateVariableImpl();
      setApiVarProps(apiVar, var);
      apiVar.setBinWidth(dateVar.getBinSize());
      apiVar.setBinWidthOverride(null);
      apiVar.setBinUnits(dateVar.getBinUnits());
      apiVar.setDisplayRangeMin(dateVar.getDisplayRangeMin());
      apiVar.setDisplayRangeMax(dateVar.getDisplayRangeMax());
      apiVar.setRangeMin(dateVar.getRangeMin());
      apiVar.setRangeMax(dateVar.getRangeMax());
      apiVar.setVocabulary(dateVar.getVocabulary());
      apiVar.setDistinctValuesCount(dateVar.getDistinctValuesCount());
      apiVar.setIsFeatured(dateVar.getIsFeatured());
      apiVar.setIsMergeKey(dateVar.getIsMergeKey());
      apiVar.setIsMultiValued(dateVar.getIsMultiValued());
      apiVar.setIsTemporal(dateVar.getIsTemporal());
      return apiVar;
    }
    else if (var.getType() == VariableType.NUMBER) {
      NumberVariable numVar = (NumberVariable)var;
      APINumberVariable apiVar = new APINumberVariableImpl();
      setApiVarProps(apiVar, var);
      apiVar.setUnits(numVar.getUnits());
      apiVar.setPrecision(numVar.getPrecision());
      apiVar.setBinWidth(numVar.getBinWidth());
      apiVar.setBinWidthOverride(numVar.getBinWidthOverride());
      apiVar.setDisplayRangeMin(numVar.getDisplayRangeMin());
      apiVar.setDisplayRangeMax(numVar.getDisplayRangeMax());
      apiVar.setRangeMin(numVar.getRangeMin());
      apiVar.setRangeMax(numVar.getRangeMax());
      apiVar.setDistinctValuesCount(numVar.getDistinctValuesCount());
      apiVar.setIsFeatured(numVar.getIsFeatured());
      apiVar.setIsMergeKey(numVar.getIsMergeKey());
      apiVar.setIsMultiValued(numVar.getIsMultiValued());
      apiVar.setIsTemporal(numVar.getIsTemporal());
      apiVar.setVocabulary(numVar.getVocabulary());
      return apiVar;
    }
    else if (var.getType() == VariableType.STRING) {
      StringVariable strVar = (StringVariable)var;	
      APIStringVariable apiVar = new APIStringVariableImpl();
      setApiVarProps(apiVar, var);
      apiVar.setDistinctValuesCount(strVar.getDistinctValuesCount());
      apiVar.setIsFeatured(strVar.getIsFeatured());
      apiVar.setIsMergeKey(strVar.getIsMergeKey());
      apiVar.setIsMultiValued(strVar.getIsMultiValued());
      apiVar.setIsTemporal(strVar.getIsTemporal());
      apiVar.setVocabulary(strVar.getVocabulary());
      return apiVar;
    }
    else if (var.getType() == VariableType.LONGITUDE) {
      NumberVariable numVar = (NumberVariable)var;
      APILongitudeVariable apiVar = new APILongitudeVariableImpl();
      setApiVarProps(apiVar, var);
      apiVar.setDistinctValuesCount(numVar.getDistinctValuesCount());
      apiVar.setIsFeatured(false);
      apiVar.setIsMergeKey(false);
      apiVar.setIsMultiValued(false);
      apiVar.setIsTemporal(false);
>>>>>>> template/master
      return apiVar;
    }
    else {
      throw new RuntimeException("Invalid variable type " + var.getType());
    }
  }
  
  private static void setApiVarProps(APIVariable apiVar, Variable var) {
    apiVar.setId(var.getId());
<<<<<<< HEAD
    apiVar.setDisplayName(var.getDisplayName());
    apiVar.setProviderLabel(var.getProviderLabel());
    apiVar.setParentId(var.getParentId());
=======
    apiVar.setDataShape(APIVariableDataShape.valueOf(var.getDataShape().toString()));
    apiVar.setDisplayType(APIVariableDisplayType.valueOf(var.getDisplayType().toString()));
    apiVar.setDisplayName(var.getDisplayName());
    apiVar.setProviderLabel(var.getProviderLabel());
    apiVar.setParentId(var.getParentId());
    apiVar.setDefinition(var.getDefinition());
    apiVar.setDisplayOrder(var.getDisplayOrder());
>>>>>>> template/master
  }

  @Override
  public PostStudiesEntitiesVariablesDistributionByStudyIdAndEntityIdAndVariableIdResponse 
  postStudiesEntitiesVariablesDistributionByStudyIdAndEntityIdAndVariableId(
      String studyId, String entityId, String variableId, VariableDistributionPostRequest request) {

<<<<<<< HEAD
  //  long start = System.currentTimeMillis();
    DataSource datasource = Resources.getApplicationDataSource();
   // System.out.println("---------------- 1: " + String.valueOf(System.currentTimeMillis() - start));
    
    // unpack data from API input to model objects
    List<String> vars = new ArrayList<>();
    vars.add(variableId);  // force into a list for the unpacker
    UnpackedRequest unpacked = unpack(datasource, studyId, entityId, request.getFilters(), vars);
   // System.out.println("---------------- 2: " + String.valueOf(System.currentTimeMillis() - start));

    Variable var = unpacked.getTargetEntity().getVariable(variableId)
        .orElseThrow(() -> new NotFoundException("Variable ID not found: " + variableId));
   // System.out.println("---------------- 3: " + String.valueOf(System.currentTimeMillis() - start));

    VariableDistributionPostResponseStream streamer = getDistributionResponseStreamer(
        datasource, unpacked.getStudy(), unpacked.getTargetEntity(), unpacked.getFilters(), var);
   // System.out.println("---------------- 4: " + String.valueOf(System.currentTimeMillis() - start));

    return PostStudiesEntitiesVariablesDistributionByStudyIdAndEntityIdAndVariableIdResponse.
        respond200WithApplicationJson(streamer);
  }

  public static VariableDistributionPostResponseStream getDistributionResponseStreamer(
      DataSource dataSource,
      Study study,
      Entity targetEntity,
      List<Filter> filters,
      Variable variable
  ) {
    // get entity tree pruned to those entities of current interest
    TreeNode<Entity> prunedEntityTree = StudySubsettingUtils.pruneTree(
        study.getEntityTree(), filters, targetEntity);

    // get variable count (may do in parallel later)
    int variableCount = StudySubsettingUtils.getVariableCount(
        dataSource, prunedEntityTree, targetEntity, variable, filters);

    return new VariableDistributionPostResponseStream(outStream -> {
      BufferedOutputStream bufferedOutput = new BufferedOutputStream(outStream);
      try(
        // create a stream of distribution tuples converted from a database result
        Stream<TwoTuple<String, Integer>> distributionStream =
            StudySubsettingUtils.produceVariableDistribution(
              dataSource, prunedEntityTree, targetEntity, variable, filters);
        // create a JSON generator around the output stream
        JsonGenerator json = new JsonFactory().createGenerator(bufferedOutput, JsonEncoding.UTF8)
      ) {
        // write the output
        json.writeStartObject();
        json.writeNumberField("entitiesCount", variableCount);
        json.writeFieldName("distribution");
        json.writeStartObject();
        distributionStream.forEach(cSwallow(row -> json.writeNumberField(row.getKey(), row.getValue())));
        json.writeEndObject();
        json.writeEndObject();
        json.flush();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
=======
    // unpack data from API input to model objects
    DataSource ds = Resources.getApplicationDataSource();
    RequestBundle req = RequestBundle.unpack(ds, studyId, entityId, request.getFilters(), ListBuilder.asList(variableId), null);
    VariableWithValues var = getRequestedVariable(req);

    DistributionResult result = processDistributionRequest(ds, req.getStudy(),
        req.getTargetEntity(), var, req.getFilters(), request.getValueSpec(),
        Optional.ofNullable(request.getBinSpec()));

    VariableDistributionPostResponse response = new VariableDistributionPostResponseImpl();
    response.setHistogram(result.getHistogramData());
    response.setStatistics(result.getStatistics());

    return PostStudiesEntitiesVariablesDistributionByStudyIdAndEntityIdAndVariableIdResponse.
        respond200WithApplicationJson(response);
  }

  static DistributionResult processDistributionRequest(DataSource ds, Study study, Entity targetEntity,
      VariableWithValues var, List<Filter> filters, ValueSpec valueSpec, Optional<BinSpecWithRange> incomingBinSpec) {
    // inspect requested variable and select appropriate distribution
    AbstractDistribution<?> distribution;
    if (var.getDataShape() == Variable.VariableDataShape.CONTINUOUS) {
      BinSpecWithRange binSpec = validateVarAndBinSpec(var, incomingBinSpec);
      LOG.debug("Found var of type: " + var.getType() + ", will use bin spec: " + JsonUtil.serializeObject(binSpec));
      distribution = switch(var.getType()) {
        case NUMBER -> new NumberBinDistribution(ds, study, targetEntity,
            (NumberVariable)var, filters, valueSpec, binSpec);
        case DATE -> new DateBinDistribution(ds, study, targetEntity,
            (DateVariable)var, filters, valueSpec, binSpec);
        default -> throw new BadRequestException(
            "Requested variable '" + var.getId() + "' must be a date or number, but is " + var.getType());
      };
    }
    else {
      if (incomingBinSpec.isPresent()) {
        throw new BadRequestException("Bin spec is allowed/required only for continuous variables.");
      }
      distribution = new DiscreteDistribution(ds, study, targetEntity, var, filters, valueSpec);
    }

    return distribution.generateDistribution();
  }

  private VariableWithValues getRequestedVariable(RequestBundle req) {
    if (req.getRequestedVariables().isEmpty()) {
      throw new RuntimeException("No requested variables (empty URL segment?)");
    }
    Variable var = req.getRequestedVariables().get(0);
    if (!(var instanceof VariableWithValues)) {
      throw new BadRequestException("Distribution endpoint can only be called with a variable that has values.");
    }
    return (VariableWithValues)var;
  }

  private static BinSpecWithRange validateVarAndBinSpec(Variable var, Optional<BinSpecWithRange> submittedBinSpec) {
    // bin spec is needed; type determined by var type
    switch(var.getType()) {
      case STRING:
      case LONGITUDE:
        throw new BadRequestException("Among continuous variables, " +
            "distribution endpoint supports only date and number types.");
      case DATE:
        DateVariable dateVar = (DateVariable)var;
        if (submittedBinSpec.isEmpty()) {
          // use defaults
          BinSpecWithRange binSpec = new BinSpecWithRangeImpl();
          binSpec.setDisplayRangeMin(dateVar.getRangeMin());
          binSpec.setDisplayRangeMax(dateVar.getRangeMax());
          binSpec.setBinUnits(dateVar.getDefaultBinUnits());
          binSpec.setBinWidth(dateVar.getBinSize());
          return binSpec;
        }
        else {
          BinSpecWithRange binSpec = submittedBinSpec.get();
          if (binSpec.getBinUnits() == null) {
            throw new BadRequestException("binUnits is required for date variable distributions");
          }
          if (binSpec.getBinWidth().intValue() <= 0) {
            throw new BadRequestException("binWidth must be a positive integer for date variable distributions");
          }
          return binSpec;
        }
      case NUMBER:
        NumberVariable numberVar = (NumberVariable)var;
        if (submittedBinSpec.isEmpty()) {
          // use defaults
          BinSpecWithRange binSpec = new BinSpecWithRangeImpl();
          binSpec.setDisplayRangeMin(numberVar.getDisplayRangeMin());
          binSpec.setDisplayRangeMax(numberVar.getDisplayRangeMax());
          binSpec.setBinWidth(numberVar.getDefaultBinWidth());
          return binSpec;
        }
        else {
          BinSpecWithRange binSpec = submittedBinSpec.get();
          if (binSpec.getBinUnits() != null || binSpec.getBinWidth() == null) {
            throw new BadRequestException("For number variables, only binWidth should be submitted (not binUnits).");
          }
          if (binSpec.getBinWidth().doubleValue() <= 0) {
            throw new BadRequestException("binWidth must be a positive number for number variable distributions");
          }
          return binSpec;
        }
      default:
        throw new RuntimeException("Must add case statement here for type: " + var.getType());
    }
>>>>>>> template/master
  }

  @Override
  public PostStudiesEntitiesTabularByStudyIdAndEntityIdResponse postStudiesEntitiesTabularByStudyIdAndEntityId(String studyId,
      String entityId, EntityTabularPostRequest requestBody) {
    
    DataSource datasource = Resources.getApplicationDataSource();
    
<<<<<<< HEAD
    UnpackedRequest request = unpack(datasource, studyId, entityId, requestBody.getFilters(), requestBody.getOutputVariableIds());
    
    EntityTabularPostResponseStream streamer = new EntityTabularPostResponseStream
        (outStream -> StudySubsettingUtils.produceTabularSubset(datasource, request.getStudy(),
            request.getTargetEntity(), request.getRequestedVariables(), request.getFilters(), outStream));
=======
    RequestBundle request = RequestBundle.unpack(datasource, studyId, entityId, requestBody.getFilters(), requestBody.getOutputVariableIds(), requestBody.getReportConfig());
    
    EntityTabularPostResponseStream streamer = new EntityTabularPostResponseStream
        (outStream -> StudySubsettingUtils.produceTabularSubset(datasource, request.getStudy(),
            request.getTargetEntity(), request.getRequestedVariables(), request.getFilters(),
            request.getReportConfig(), outStream));
>>>>>>> template/master

    return PostStudiesEntitiesTabularByStudyIdAndEntityIdResponse
        .respond200WithTextPlain(streamer);
  }

  @Override
  public PostStudiesEntitiesCountByStudyIdAndEntityIdResponse postStudiesEntitiesCountByStudyIdAndEntityId(
      String studyId, String entityId, EntityCountPostRequest rawRequest) {

    DataSource datasource = Resources.getApplicationDataSource();

    // unpack data from API input to model objects
<<<<<<< HEAD
    List<String> vars = new ArrayList<>();
    UnpackedRequest request = unpack(datasource, studyId, entityId, rawRequest.getFilters(), vars);
=======
	RequestBundle request = RequestBundle.unpack(datasource, studyId, entityId, rawRequest.getFilters(), Collections.emptyList(), null);
>>>>>>> template/master

    TreeNode<Entity> prunedEntityTree = StudySubsettingUtils.pruneTree(
        request.getStudy().getEntityTree(), request.getFilters(), request.getTargetEntity());

    int count = StudySubsettingUtils.getEntityCount(
        datasource, prunedEntityTree, request.getTargetEntity(), request.getFilters());

    EntityCountPostResponse response = new EntityCountPostResponseImpl();
    response.setCount(count);

    return  PostStudiesEntitiesCountByStudyIdAndEntityIdResponse.respond200WithApplicationJson(response);
  }
<<<<<<< HEAD
  
  private UnpackedRequest unpack(DataSource datasource, String studyId, String entityId, List<APIFilter> apiFilters, List<String> variableIds) {
    String studIdStr = "Study ID " + studyId;
    if (!validateStudyId(datasource, studyId))
      throw new NotFoundException(studIdStr + " is not found.");
   
    Study study = getStudy(studyId);
    Entity entity = study.getEntity(entityId).orElseThrow(() -> new NotFoundException("In " + studIdStr + " Entity ID not found: " + entityId));
    
    List<Variable> variables = getEntityVariables(entity, variableIds);

    List<Filter> filters = constructFiltersFromAPIFilters(study, apiFilters);
  
    return new UnpackedRequest(study, entity, variables, filters);
  }
  /*
   * return true if valid study id
   */
  public boolean validateStudyId(DataSource datasource, String studyId) {
    return getStudyOverviews(datasource).stream()
            .anyMatch(study -> study.getId().equals(studyId));
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
      
      // validate filter's variable id
      String varId = apiFilter.getVariableId();
      entity.getVariable(varId).orElseThrow(() -> new BadRequestException("Variable '" + varId + "' is not found"));

      Filter newFilter;
      if (apiFilter instanceof APIDateRangeFilter) {
        APIDateRangeFilter f = (APIDateRangeFilter)apiFilter;
        newFilter = new DateRangeFilter(entity, varId,
            convertDate(f.getMin()), convertDate(f.getMax()));
      } else if (apiFilter instanceof APIDateSetFilter) {
        APIDateSetFilter f = (APIDateSetFilter)apiFilter;
        List<LocalDateTime> dateSet = new ArrayList<>();
        for (String dateStr : f.getDateSet()) dateSet.add(convertDate(dateStr));
        newFilter = new DateSetFilter(entity, varId, dateSet);
      } else if (apiFilter instanceof APINumberRangeFilter) {
        APINumberRangeFilter f = (APINumberRangeFilter)apiFilter;
        newFilter = new NumberRangeFilter(entity, varId, f.getMin(), f.getMax());
      } else if (apiFilter instanceof APINumberSetFilter) {
        APINumberSetFilter f = (APINumberSetFilter)apiFilter;
        newFilter = new NumberSetFilter(entity, varId, f.getNumberSet());
      } else if (apiFilter instanceof APILongitudeRangeFilter) {
        APILongitudeRangeFilter f = (APILongitudeRangeFilter)apiFilter;
        newFilter = new LongitudeRangeFilter(entity, varId, f.getLeft(), f.getRight());
      } else if (apiFilter instanceof APIStringSetFilter) {
        APIStringSetFilter f = (APIStringSetFilter)apiFilter;
        newFilter = new StringSetFilter(entity, varId, f.getStringSet());
      } else
        throw new InternalServerErrorException("Input filter not an expected subclass of Filter");

      subsetFilters.add(newFilter);
    }
    return subsetFilters;
  }

  static List<Variable> getEntityVariables(Entity entity, List<String> variableIds) {

    List<Variable> variables = new ArrayList<>();
    
    for (String varId : variableIds) {
      String errMsg = "Variable '" + varId + "' is not found for entity with ID: '" + entity.getId() + "'";
      variables.add(entity.getVariable(varId).orElseThrow(() -> new BadRequestException(errMsg)));
    }
    return variables;
  }
  
  static LocalDateTime convertDate(String dateStr) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
      return LocalDateTime.parse(dateStr, formatter);
    }
    catch (DateTimeParseException e) {
      throw new BadRequestException("Can't parse date string" + dateStr);
    }
  }

  static class UnpackedRequest {

    private final Study _study;
    private final List<Filter> _filters;
    private final Entity _targetEntity;
    private final List<Variable> _requestedVariables;

    UnpackedRequest(Study study, Entity targetEntity, List<Variable> requestedVariables, List<Filter> filters) {
      _study = study;
      _targetEntity = targetEntity;
      _filters = filters;
      _requestedVariables = requestedVariables;
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
  }
=======
>>>>>>> template/master
}
