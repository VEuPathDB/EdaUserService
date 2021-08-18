package org.veupathdb.service.eda.ss.model;

<<<<<<< HEAD
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;

import java.sql.ResultSet;

public class Variable {
=======
public class Variable {

>>>>>>> template/master
  private final String providerLabel;
  private final String id;
  private final Entity entity;
  private final VariableType type;
  private final VariableDataShape dataShape;
  private final VariableDisplayType displayType;
<<<<<<< HEAD
  private final boolean hasValues;
  private final String units;
  private final Integer precision;
  private final String displayName;
  private final String parentId;

  public boolean getHasValues() {
    return hasValues;
  }

  public VariableDisplayType getDisplayType() {
    return displayType;
  }

  public enum VariableType {
    STRING ("string_value", rs -> rs.getString("string_value"), "string"),  
    NUMBER ("number_value", rs -> String.valueOf(rs.getDouble("number_value")), "number"),
    DATE   ("date_value", rs -> FormatUtil.formatDateTimeNoTimezone(rs.getDate("date_value")), "date"),
    LONGITUDE ("number_value", rs -> String.valueOf(rs.getDouble("number_value")), "longitude");

    private final String tallTableColumnName;
    private final String typeString;
    private final FunctionWithException<ResultSet, String> resultSetToStringValue;

    VariableType(String tallTableColumnName, FunctionWithException<ResultSet, String> resultSetToStringValue, String typeString) {
      this.tallTableColumnName = tallTableColumnName;
      this.resultSetToStringValue = resultSetToStringValue;
      this.typeString = typeString;
    }
    
    public String getTallTableColumnName() {
      return this.tallTableColumnName;
    }

    public static VariableType fromString(String str) {
      if (str.equals(STRING.typeString) || str.equals("boolean")) return STRING;  // TODO remove boolean hack
      else if (str.equals(NUMBER.typeString)) return NUMBER;
      else if (str.equals(LONGITUDE.typeString)) return LONGITUDE;
      else if (str.equals(DATE.typeString)) return DATE;
      else throw new RuntimeException("Illegal variable type string: " + str);
    }
    
    public String convertRowValueToStringValue(ResultSet rs) {
      try {
        return resultSetToStringValue.apply(rs);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
=======
  private final String displayName;
  private final String parentId;
  private final String definition;
  private final boolean hasValues;

  private final Integer displayOrder;
>>>>>>> template/master

  private final static String CONT_STR = "continuous";
  private final static String CAT_STR = "categorical";
  private final static String ORD_STR = "ordinal";
  private final static String TWO_STR = "binary";

  public enum VariableDataShape {
    CONTINUOUS(CONT_STR),
    CATEGORICAL(CAT_STR),
    ORDINAL(ORD_STR),
    BINARY(TWO_STR);

    private final String name;

    VariableDataShape(String name) {
      this.name = name;
    }

    public static VariableDataShape fromString(String shapeString) {

      VariableDataShape v;

      switch (shapeString) {
        case CONT_STR -> v = CONTINUOUS;
        case CAT_STR -> v = CATEGORICAL;
        case ORD_STR -> v = ORDINAL;
        case TWO_STR -> v = BINARY;
        default -> throw new RuntimeException("Unrecognized data shape: " + shapeString);
      }
      return v;
    }

    public String getName() { return name;}
  }

  public enum VariableDisplayType {
    DEFAULT("default"),
    HIDDEN("hidden"),
    MULTIFILTER("multifilter");

    String type;

    VariableDisplayType(String type) {
      this.type = type;
    }

    public static VariableDisplayType fromString(String displayType) {

      VariableDisplayType t;

      switch (displayType) {
        case "default" -> t = DEFAULT;
        case "multifilter" -> t = MULTIFILTER;
        case "hidden" -> t = HIDDEN;
        default -> throw new RuntimeException("Unrecognized variable display type: " + displayType);
      }
      return t;
    }

    public String getType() { return type; }
  }

  /*
<<<<<<< HEAD
  Construct a variable that does have values
   */
  public Variable(String providerLabel, String id, Entity entity, VariableType type, VariableDataShape dataShape,
                  VariableDisplayType displayType, String units, Integer precision, String displayName, String parentId) {

    String errPrefix = "In entity " + entity.getId() + " variable " + id + " has a null ";
    if (type == null) throw new RuntimeException(errPrefix + "data type");
    if (dataShape == null) throw new RuntimeException(errPrefix + "data shape");
    if (displayType == null) throw new RuntimeException(errPrefix + "display type");
    if (type.equals(VariableType.NUMBER)) {
      if (units == null) throw new RuntimeException(errPrefix + "units");
      if (precision == null) throw new RuntimeException(errPrefix + "precision");
    }

    this.providerLabel = providerLabel;
    this.id = id;
    this.entity = entity;
    this.type = type;
    this.dataShape = dataShape;
    this.displayType = displayType;
    this.hasValues = true;
    this.units = units;
    this.precision = precision;
    this.displayName = displayName;
    this.parentId = parentId;
=======
  Construct a variable that has values
   */
  public Variable(String providerLabel, String id, Entity entity, VariableType type, VariableDataShape shape, VariableDisplayType displayType, String displayName, Integer displayOrder, String parentId, String definition) {
	  this(providerLabel, id, entity, true, type, shape, displayType, displayName, displayOrder, parentId, definition);
>>>>>>> template/master
  }

  /*
  Construct a variable that does not have values
   */
<<<<<<< HEAD
  public Variable(String providerLabel, String id, Entity entity, String displayName, String parentId) {
    this.providerLabel = providerLabel;
    this.id = id;
    this.entity = entity;
    this.type = null;
    this.dataShape = null;
    this.displayType = null;
    this.hasValues = false;
    this.units = null;
    this.precision = null;
    this.displayName = displayName;
    this.parentId = parentId;

  }

=======
  public Variable(String providerLabel, String id, Entity entity, VariableDisplayType displayType, String displayName, Integer displayOrder, String parentId) {
	  this(providerLabel, id, entity, false, null, null, displayType, displayName, displayOrder, parentId, null);
  }

  private Variable(String providerLabel, String id, Entity entity, boolean hasValues, VariableType type, VariableDataShape shape, VariableDisplayType displayType, String displayName, Integer displayOrder, String parentId, String definition) {
	    this.providerLabel = providerLabel;
	    this.id = id;
	    this.entity = entity;
	    this.type = type;
	    this.dataShape = shape;
	    this.displayType = displayType;
	    this.hasValues = hasValues;
	    this.displayName = displayName;
	    this.displayOrder = displayOrder;
	    this.parentId = parentId;
	    this.definition= definition;
	  }
  
>>>>>>> template/master
  public String getProviderLabel() {
    return providerLabel;
  }

  public String getId() {
    return id;
  }

<<<<<<< HEAD
=======
  public boolean getHasValues() {
	return hasValues;
  }

  public VariableDisplayType getDisplayType() {
	return displayType;
  }

>>>>>>> template/master
  public String getEntityId() {
    return entity.getId();
  } 
  
  public Entity getEntity() {
    return entity;
  } 
  
  public VariableDataShape getDataShape() {
    return dataShape;
  }
  
<<<<<<< HEAD
  public String getUnits() {
    return units;
  }

  public Integer getPrecision() {
    return precision;
  }

=======
>>>>>>> template/master
  public String getDisplayName() {
    return displayName;
  }

  public String getParentId() {
    return parentId;
  }

  public VariableType getType() {
    return type;
  }
<<<<<<< HEAD
=======
  
  public String getDefinition() {
		return definition;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

>>>>>>> template/master
}
