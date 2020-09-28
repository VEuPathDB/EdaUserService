/**
 * 
 */
package org.veupathdb.service.edass.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Steve
 *
 */
public class Entity {
  private String entityId;
  private String entityName;
  private String entityTallTableName;
  private String entityAncestorsTableName;
  private String entityPrimaryKeyColumnName;
  private List<Entity> ancestorEntities;
  private List<String> ancestorPkColNames;
  private List<String> ancestorFullPkColNames; // entityName.pkColName
  
  public Entity(String entityName, String entityId, String entityTallTableName, String entityAncestorsTableName,
      String entityPrimaryKeyColumnName) {
    this.entityTallTableName = entityTallTableName;
    this.entityAncestorsTableName = entityAncestorsTableName;
    this.entityPrimaryKeyColumnName = entityPrimaryKeyColumnName;
  }

  String getEntityId() {
    return entityId;
  }

  String getEntityName() {
    return entityName;
  }

  String getEntityTallTableName() {
    return entityTallTableName;
  }

  String getEntityPrimaryKeyColumnName() {
    return entityPrimaryKeyColumnName;
  }
  
  public String getEntityParentTableName() {
    return entityAncestorsTableName;
  }
  
  public List<String> getAncestorPkColNames() {
    return Collections.unmodifiableList(ancestorPkColNames);
  }
  
  public List<String> getAncestorFullPkColNames() {
    return Collections.unmodifiableList(ancestorFullPkColNames);
  }
  
  public void setAncestorEntities(List<Entity> ancestorEntities) {
    this.ancestorEntities = ancestorEntities;
    this.ancestorPkColNames = 
        ancestorEntities.stream().map(entry -> entry.getEntityPrimaryKeyColumnName()).collect(Collectors.toList());
    this.ancestorFullPkColNames = 
        ancestorEntities.stream().map(entry -> entry.getEntityName() + "." + entry.getEntityPrimaryKeyColumnName()).collect(Collectors.toList());
  }

  public List<Entity> getAncestorEntities() {
    return Collections.unmodifiableList(ancestorEntities);
  }
}
