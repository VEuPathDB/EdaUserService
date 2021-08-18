package org.veupathdb.service.eda.ss.model.distribution;

import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.TreeNode;
import org.veupathdb.service.eda.generated.model.ValueSpec;
import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.Study;
import org.veupathdb.service.eda.ss.model.StudySubsettingUtils;
import org.veupathdb.service.eda.ss.model.VariableWithValues;
import org.veupathdb.service.eda.ss.model.filter.Filter;

public abstract class AbstractDistribution<T extends VariableWithValues> {

  // used to produce the stream of distribution tuples
  private final DataSource _ds;
  private final Study _study;
  private final Entity _targetEntity;
  protected final T _variable;
  private final List<Filter> _filters;

  // used to tailor the response to either count or proportion values
  protected final ValueSpec _valueSpec;

  /**
   * Build a distribution result from the passed stream; unique entity count is provided
   * @param distributionStream stream of tuples for processing
   * @param subsetEntityCount number of entities in the subset
   * @return distribution result
   */
  protected abstract DistributionResult processDistributionStream(
      Stream<TwoTuple<String, Long>> distributionStream, int subsetEntityCount);

  public AbstractDistribution(DataSource ds, Study study, Entity targetEntity, T variable,
      List<Filter> filters, ValueSpec valueSpec) {
    _ds = ds;
    _study = study;
    _targetEntity = targetEntity;
    _variable = variable;
    _filters = filters;
    _valueSpec = valueSpec;
  }

  public DistributionResult generateDistribution() {

    // get entity tree pruned to those entities of current interest
    TreeNode<Entity> prunedEntityTree = StudySubsettingUtils.pruneTree(
        _study.getEntityTree(), _filters, _targetEntity);

    // get the number of entities in the subset
    int subsetEntityCount = StudySubsettingUtils.getEntityCount(
        _ds, prunedEntityTree, _targetEntity, _filters);

    try(
      // create a stream of distribution tuples converted from a database result
      Stream<TwoTuple<String, Long>> distributionStream =
          StudySubsettingUtils.produceVariableDistribution(
            _ds, prunedEntityTree, _targetEntity, _variable, _filters)
    ) {
      return processDistributionStream(distributionStream, subsetEntityCount);
    }
  }
}
