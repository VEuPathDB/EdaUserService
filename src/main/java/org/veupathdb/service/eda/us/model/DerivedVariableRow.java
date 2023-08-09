package org.veupathdb.service.eda.us.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.veupathdb.service.eda.generated.model.DerivedVariableGetResponse;
import org.veupathdb.service.eda.generated.model.DerivedVariableGetResponseImpl;
import org.veupathdb.service.eda.generated.model.DerivedVariablePostRequest;
import org.veupathdb.service.eda.us.Utils;

import static org.veupathdb.service.eda.us.Utils.apply;

/**
 * Represents a complete database row for a Derived Variable.
 */
public class DerivedVariableRow {

  private String variableID;
  private long userID;
  private String datasetID;
  private String entityID;
  private String displayName;
  private String description;
  private JsonNode provenance;
  private String functionName;
  private JsonNode config;

  public DerivedVariableRow(
    String variableID,
    long userID,
    String datasetID,
    String entityID,
    String displayName,
    String description,
    JsonNode provenance,
    String functionName,
    JsonNode config
  ) {
    this.variableID = variableID;
    this.userID = userID;
    this.datasetID = datasetID;
    this.entityID = entityID;
    this.displayName = displayName;
    this.description = description;
    this.provenance = provenance;
    this.functionName = functionName;
    this.config = config;
  }

  public DerivedVariableRow(String variableID, long userID, DerivedVariablePostRequest request) {
    this.variableID = variableID;
    this.userID = userID;
    this.datasetID = request.getDatasetId();
    this.entityID = request.getEntityId();
    this.displayName = request.getDisplayName();
    this.description = request.getDescription();
    this.provenance = request.getProvenance() == null ?
      null : Utils.JSON.convertValue(request.getProvenance(), JsonNode.class);
    this.functionName = request.getFunctionName();
    this.config = Utils.JSON.convertValue(request.getConfig(), JsonNode.class);
  }

  public String getVariableID() {
    return variableID;
  }

  public long getUserID() {
    return userID;
  }

  public String getDatasetID() {
    return datasetID;
  }

  public String getEntityID() {
    return entityID;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  public JsonNode getProvenance() {
    return provenance;
  }

  public String getFunctionName() {
    return functionName;
  }

  public JsonNode getConfig() {
    return config;
  }

  public void setVariableID(String variableID) {
    this.variableID = variableID;
  }

  public void setUserID(long userID) {
    this.userID = userID;
  }

  public void setDatasetID(String datasetID) {
    this.datasetID = datasetID;
  }

  public void setEntityID(String entityID) {
    this.entityID = entityID;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setProvenance(JsonNode provenance) {
    this.provenance = provenance;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public void setConfig(JsonNode config) {
    this.config = config;
  }

  public DerivedVariableGetResponse toGetResponse() {
    return apply(new DerivedVariableGetResponseImpl(), out -> {
      out.setVariableId(variableID);
      out.setDatasetId(datasetID);
      out.setEntityId(entityID);
      out.setDisplayName(displayName);
      out.setDescription(description);
      out.setProvenance(provenance);
      out.setFunctionName(functionName);
      out.setConfig(config);
    });
  }
}
