package org.veupathdb.service.edass.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;
import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.FormatUtil.TAB;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SingleIntResultSetHandler;
import org.gusdb.fgputil.db.stream.ResultSetIterator;
import org.gusdb.fgputil.db.stream.ResultSets;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.iterator.GroupingIterator;
import org.gusdb.fgputil.iterator.IteratorUtil;
import org.veupathdb.service.edass.Resources;
import org.veupathdb.service.edass.model.Variable.VariableType;

import static org.veupathdb.service.edass.model.RdbmsColumnNames.TT_VARIABLE_ID_COL_NAME;

import org.apache.logging.log4j.Logger;

/**
 * A class to perform subsetting operations on a study entity
 * 
 * @author Steve
 *
 */
public class StudySubsettingUtils {

  private static final String VALUE_COLUMN_NAME = "value";
  private static final String COUNT_COLUMN_NAME = "count";

  /**
   * Writes to the passed output stream tab delimited records containing
   * the requested variables of the specified entity, reduced to the
   * subset created by applying the given filters.
   *
   * @param datasource DB to run against
   * @param study study context
   * @param outputEntity entity type to return
   * @param outputVariables variables requested
   * @param filters filters to apply to create a subset of records
   */
  public static void produceTabularSubset(DataSource datasource, Study study, Entity outputEntity,
      List<Variable> outputVariables, List<Filter> filters, OutputStream outputStream) {

    TreeNode<Entity> prunedEntityTree = pruneTree(study.getEntityTree(), filters, outputEntity);

    String sql = generateTabularSql(outputVariables, outputEntity, filters, prunedEntityTree);

    List<String> outputVariableIds = outputVariables.stream().map(Variable::getId).collect(Collectors.toList());

    List<String> outputColumns = new ArrayList<>();
    outputColumns.add(outputEntity.getPKColName());
    outputColumns.addAll(outputEntity.getAncestorPkColNames());
    outputColumns.addAll(outputVariableIds);

    new SQLRunner(datasource, sql).executeQuery(rs -> {
      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {

        writer.write(String.join(TAB, outputColumns) + NL);
        writeWideRows(rs, writer, outputColumns, outputEntity);
        writer.flush();
        return null;
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
  
  private static void writeWideRows(ResultSet rs, Writer writer, List<String> outputColumns, Entity outputEntity) throws IOException {

    // an interator of maps, each representing a single row in the tall table
    Iterator<Map<String, String>> tallRowsIterator = new ResultSetIterator<>(rs,
        row -> Optional.of(EntityResultSetUtils.resultSetToTallRowMap(outputEntity, rs)));

    // an iterator of lists of maps, each list being the rows of the tall table returned for a single entity id
    String pkCol = outputEntity.getPKColName();
    Iterator<List<Map<String, String>>> groupedTallRowsIterator = new GroupingIterator<>(
        tallRowsIterator, (row1, row2) -> row1.get(pkCol).equals(row2.get(pkCol)));

    // iterate through groups and format into strings to be written to stream
    for (List<Map<String, String>> group : IteratorUtil.toIterable(groupedTallRowsIterator)) {
      Map<String, String> wideRowMap = EntityResultSetUtils.getTallToWideFunction(outputEntity).apply(group);
      List<String> wideRow = new ArrayList<>();
      for (String colName : outputColumns) {
        wideRow.add(wideRowMap.getOrDefault(colName, ""));
      }
      writer.write(String.join(TAB, wideRow) + NL);
    }
  }

  /**
   * NOTE! This stream MUST be closed by the caller once the stream has been processed.
   * The easiest way to do this is with a try-with-resources around this method's call.
   * @return stream of distribution tuples
   */
  public static Stream<TwoTuple<String,Integer>> produceVariableDistribution(
      DataSource datasource, TreeNode<Entity> prunedEntityTree, Entity outputEntity,
      Variable distributionVariable, List<Filter> filters) {
    String sql = generateDistributionSql(outputEntity, distributionVariable, filters, prunedEntityTree);
    return ResultSets.openStream(datasource, sql, row -> Optional.of(

        new TwoTuple<>(distributionVariable.getType().convertRowValueToStringValue(row), row.getInt(COUNT_COLUMN_NAME))));
  }

  public static int getVariableCount(
      DataSource datasource, TreeNode<Entity> prunedEntityTree, Entity outputEntity,
      Variable distributionVariable, List<Filter> filters) {
    String sql = generateVariableCountSql(outputEntity, distributionVariable, filters, prunedEntityTree);
    return new SQLRunner(datasource, sql).executeQuery(new SingleIntResultSetHandler());
  }

  public static int getEntityCount(
      DataSource datasource, TreeNode<Entity> prunedEntityTree, Entity targetEntity, List<Filter> filters) {
    String sql = generateEntityCountSql(targetEntity, filters, prunedEntityTree);
    return new SQLRunner(datasource, sql).executeQuery(new SingleIntResultSetHandler());
  }

  /**
   * Prune tree to include only active nodes, based on filters and output entity
   */
  public static TreeNode<Entity> pruneTree(TreeNode<Entity> tree, List<Filter> filters, Entity outputEntity) {

    List<String> entityIdsInFilters = getEntityIdsInFilters(filters);

    Predicate<Entity> isActive =
        e -> entityIdsInFilters.contains(e.getId()) ||
        e.getId().equals(outputEntity.getId());

    return pruneToActiveAndPivotNodes(tree, isActive);
  }
  
  static List<String> getEntityIdsInFilters(List<Filter> filters) {
    return filters.stream().map(f -> f.getEntity().getId()).collect(Collectors.toList());
  }

  /**
   * Generate SQL to produce a multi-column tabular output (the requested variables), for the specified subset.
   */
  static String generateTabularSql(List<Variable> outputVariables, Entity outputEntity, List<Filter> filters, TreeNode<Entity> prunedEntityTree) {

    String tallTblAbbrev = "t"; 
    String ancestorTblAbbrev = "a";
    
    return generateWithClauses(prunedEntityTree, filters) + NL
        + generateTabularSelectClause(outputEntity, tallTblAbbrev, ancestorTblAbbrev) + NL
        + generateTabularFromClause(outputEntity, tallTblAbbrev, ancestorTblAbbrev) + NL
        + generateTabularWhereClause(outputVariables, outputEntity.getPKColName(), tallTblAbbrev, ancestorTblAbbrev) + NL
        + generateInClause(prunedEntityTree, outputEntity, tallTblAbbrev) + NL
        + generateTabularOrderByClause(outputEntity) + NL;
  }

  /*
   * Generate SQL to produce a multi-column tabular output (the requested variables), for the specified subset.
   */
  static String generateEntityCountSql(Entity outputEntity, List<Filter> filters, TreeNode<Entity> prunedEntityTree) {
    
    return generateWithClauses(prunedEntityTree, filters) + NL
        + "SELECT count(distinct " + outputEntity.getPKColName() + ") as " + COUNT_COLUMN_NAME + NL
        + "FROM (" + NL
        + generateJoiningSubselect(prunedEntityTree, outputEntity) + NL
        + ") t";
  }
  
  /**
   * Generate SQL to produce a distribution for a single variable, for the specified subset.
   */
  static String generateDistributionSql(Entity outputEntity, Variable distributionVariable, List<Filter> filters, TreeNode<Entity> prunedEntityTree) {
    
    return generateWithClauses(prunedEntityTree, filters) + NL
        + generateDistributionSelectClause(distributionVariable) + NL
        + generateDistributionFromClause(outputEntity) + NL
        + generateDistributionWhereClause(distributionVariable) + NL
        + generateInClause(prunedEntityTree, outputEntity, outputEntity.getTallTableName()) + NL
        + generateDistributionGroupByClause(distributionVariable) + NL
        + "ORDER BY " + distributionVariable.getType().getTallTableColumnName() + " ASC";
   }
  
  /**
   * Generate SQL to produce a count of the entities that have a value for a variable, for the specified subset.
   */
  static String generateVariableCountSql(Entity outputEntity, Variable variable, List<Filter> filters, TreeNode<Entity> prunedEntityTree) {
    
    return generateWithClauses(prunedEntityTree, filters) + NL
        + generateVariableCountSelectClause(variable) + NL
        + generateDistributionFromClause(outputEntity) + NL
        + generateDistributionWhereClause(variable) + NL
        + generateInClause(prunedEntityTree, outputEntity, outputEntity.getTallTableName());

   }

  static String generateWithClauses(TreeNode<Entity> prunedEntityTree, List<Filter> filters) {
    List<String> withClauses = prunedEntityTree.flatten().stream().map(e -> generateWithClause(e, filters)).collect(Collectors.toList());
    return "WITH" + NL
        + String.join("," + NL, withClauses);
  }
  
  /*
   * Get a with clause for this entity.  If the filters don't include any from this entity,
   * then the with clause will just select * from the entity's ancestor table
   */
  static String generateWithClause(Entity entity, List<Filter> filters) {

    List<String> selectColsList = new ArrayList<>(entity.getAncestorPkColNames());
    selectColsList.add(entity.getPKColName());
    String selectCols = String.join(", ", selectColsList);
    
    // default WITH body assumes no filters. we use the ancestor table because it is small
    String withBody = "  SELECT " + selectCols + " FROM " + Resources.getAppDbSchema() + entity.getAncestorsTableName() + NL;
    
    List<Filter> filtersOnThisEntity = filters.stream().filter(f -> f.getEntity().getId().equals(entity.getId())).collect(Collectors.toList());

    if (!filtersOnThisEntity.isEmpty()) {
      List<String> filterSqls = filters.stream().filter(f -> f.getEntity().getId().equals(entity.getId())).map(Filter::getSql).collect(Collectors.toList());
      withBody = String.join("INTERSECT" + NL, filterSqls);
    } 

    return entity.getWithClauseName() + " as (" + NL + withBody + ")";
  }
  
  static String generateTabularSelectClause(Entity outputEntity, String tallTblAbbrev, String ancestorTblAbbrev) {
    Set<String> valColNames = new HashSet<>();
    for (VariableType varType : VariableType.values()) valColNames.add(varType.getTallTableColumnName());

    return "SELECT " + outputEntity.getAllPksSelectList(tallTblAbbrev, ancestorTblAbbrev) + ", " + 
    TT_VARIABLE_ID_COL_NAME + ", " +
    String.join(", ", valColNames);
  }
    
  static String generateDistributionSelectClause(Variable distributionVariable) {
   // return "SELECT " + distributionVariable.getType().getTallTableColumnName() + " as " + VALUE_COLUMN_NAME + ", count(" + distributionVariable.getEntity().getPKColName() + ") as " + COUNT_COLUMN_NAME;
    return "SELECT " + distributionVariable.getType().getTallTableColumnName() + ", count(" + distributionVariable.getEntity().getPKColName() + ") as " + COUNT_COLUMN_NAME;
  }
  
  static String generateVariableCountSelectClause(Variable variable) {
    return "SELECT count(distinct " + variable.getEntity().getPKColName() + ") as " + COUNT_COLUMN_NAME;
  }
  
  static String generateDistributionFromClause(Entity outputEntity) {
    return "FROM " + Resources.getAppDbSchema() + outputEntity.getTallTableName();
  }
  
  static String generateTabularFromClause(Entity outputEntity, String entityTblNm, String ancestorTblNm) {
    return "FROM " + Resources.getAppDbSchema() + outputEntity.getTallTableName() + " " + entityTblNm + ", " +
        Resources.getAppDbSchema() + outputEntity.getAncestorsTableName() + " " + ancestorTblNm;
  }
  
  static String generateTabularWhereClause(List<Variable> outputVariables, String entityPkCol, String entityTblNm, String ancestorTblNm) {
    
    List<String> outputVariableIds = outputVariables.stream().map(Variable::getId).collect(Collectors.toList());

    List<String> varExprs = new ArrayList<>();
    for (String varId : outputVariableIds) varExprs.add(" " + TT_VARIABLE_ID_COL_NAME + " = '" + varId + "'");
    return "WHERE " + entityTblNm + "." + entityPkCol + " = " + ancestorTblNm + "." + entityPkCol + NL + (
        varExprs.isEmpty() ? "" : "AND (" + NL + String.join(" OR" + NL, varExprs) + NL + ")" + NL);
  }
  
  static String generateDistributionWhereClause(Variable outputVariable) {
    return "WHERE " + TT_VARIABLE_ID_COL_NAME + " = '" + outputVariable.getId() + "'";
  }

  static String generateInClause(TreeNode<Entity> prunedEntityTree, Entity outputEntity, String tallTblAbbrev) {
    return "AND" + " " + tallTblAbbrev + "." + outputEntity.getPKColName() + " IN (" + NL
    + generateJoiningSubselect(prunedEntityTree, outputEntity) + NL
    + ")";
  }
  
  static String generateJoiningSubselect(TreeNode<Entity> prunedEntityTree, Entity outputEntity) {
    return generateJoiningSelectClause(outputEntity) + NL
    + generateJoiningFromClause(prunedEntityTree) + NL
    + generateJoiningJoinsClause(prunedEntityTree); 
  }
  
  static String generateJoiningSelectClause(Entity outputEntity) {
    return "  SELECT " + outputEntity.getFullPKColName();
  }
  
  static String generateJoiningFromClause(TreeNode<Entity> prunedEntityTree) {
    List<String> fromClauses = prunedEntityTree.flatten().stream().map(Entity::getWithClauseName).collect(Collectors.toList());
    return "  FROM " + String.join(", ", fromClauses);
  }

  static String generateJoiningJoinsClause(TreeNode<Entity> prunedEntityTree) {
    List<String> sqlJoinStrings = new ArrayList<>();
    addSqlJoinStrings(prunedEntityTree, sqlJoinStrings);
    return sqlJoinStrings.isEmpty()? "" : "  WHERE " + String.join(NL + "  AND ", sqlJoinStrings);
  }
  
  /*
   * Add to the input list the sql join of a parent entity with each of its children, plus, recursively, its
   * children's sql joins.  (Because the tree might have been pruned, the "parent" is some ancestor)
   */
  static void addSqlJoinStrings(TreeNode<Entity> parent, List<String> sqlJoinStrings) {
    for (TreeNode<Entity> child : parent.getChildNodes()) {
      sqlJoinStrings.add(getSqlJoinString(parent.getContents(), child.getContents()));
      addSqlJoinStrings(child, sqlJoinStrings);
    }
  }

  // this join is formed using the name from the WITH clause, which is the entity name
  static String getSqlJoinString(Entity parentEntity, Entity childEntity) {
    return parentEntity.getWithClauseName() + "." + parentEntity.getPKColName() + " = " +
        childEntity.getWithClauseName() + "." + parentEntity.getPKColName();
  }
  
  static String generateTabularOrderByClause(Entity outputEntity) {
    List<String> cols = new ArrayList<>(outputEntity.getAncestorPkColNames());
    cols.add(outputEntity.getPKColName());
    return "ORDER BY " + String.join(", ", cols);
  }
  
  static String generateDistributionGroupByClause(Variable outputVariable) {
    return "GROUP BY " + outputVariable.getType().getTallTableColumnName();
  }
  

  /*
   * PRUNE THE COMPLETE TREE TO JUST THE "ACTIVE" ENTITIES WE WANT FOR OUR JOINS
   * 
   * definition: an active entity is one that must be included in the SQL definition: an active subtree is one
   * in which any entities in the subtree are active.
   * 
   * this entity is active if any of these apply: 1. it has filters 2. it is the output entity 3. it is
   * neither of the above, but has more than one child that is the root of an active subtree
   * 
   * (criterion 3 lets us join elements across connected subtrees)
   * 
   * ----X---- | | --I-- I | | | A I A
   * 
   * In the picture above the A entities are active and I are inactive. X has two children that are active
   * subtrees. We need to force X to be active so that we can join the lower A entities.
   *
   * So will we now have this:
   * 
   * ----A---- | | --I-- I | | | A I A
   * 
   * Finally, we want to prune the tree of inactive nodes, so we have the minimal active tree:
   * 
   * ----A---- | | A A
   * 
   * Now we can ascend the tree and form the concise SQL joins we need
   * 
   * Using a concrete example: ----H---- | | --P-- E | | | O S T
   * 
   * If O and and T are active (have filters or are the output entity), then we ultimately need this join:
   * where O.H_id = H.H_id and T.H_id = H.H_id
   * 
   * (The graceful implementation below is courtesy of Ryan)
   */
  private static TreeNode<Entity> pruneToActiveAndPivotNodes(TreeNode<Entity> root, Predicate<Entity> isActive) {
    return root.mapStructure((nodeContents, mappedChildren) -> {
      List<TreeNode<Entity>> activeChildren = mappedChildren.stream()
        .filter(Objects::nonNull) // filter dead branches
        .collect(Collectors.toList());
      return isActive.test(nodeContents) || activeChildren.size() > 1 ?
        // this node is active itself or a pivot node; return with any active children
        new TreeNode<>(nodeContents).addAllChildNodes(activeChildren) :
        // inactive, non-pivot node; return single active child or null
        activeChildren.isEmpty() ? null : activeChildren.get(0);
    });
  }

}
