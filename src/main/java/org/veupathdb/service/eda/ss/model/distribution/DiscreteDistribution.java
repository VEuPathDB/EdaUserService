package org.veupathdb.service.eda.ss.model.distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.iterator.IteratorUtil;
import org.veupathdb.service.eda.generated.model.HistogramBin;
import org.veupathdb.service.eda.generated.model.HistogramBinImpl;
import org.veupathdb.service.eda.generated.model.HistogramStats;
import org.veupathdb.service.eda.generated.model.HistogramStatsImpl;
import org.veupathdb.service.eda.generated.model.ValueSpec;
import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.Study;
import org.veupathdb.service.eda.ss.model.VariableWithValues;
import org.veupathdb.service.eda.ss.model.filter.Filter;

public class DiscreteDistribution extends AbstractDistribution<VariableWithValues> {

  public DiscreteDistribution(DataSource ds, Study study, Entity targetEntity,
                              VariableWithValues var, List<Filter> filters, ValueSpec valueSpec) {
    super(ds, study, targetEntity, var, filters, valueSpec);
  }

  @Override
  protected DistributionResult processDistributionStream(Stream<TwoTuple<String,Long>> distributionStream, int subsetEntityCount) {
    List<HistogramBin> bins = new ArrayList<>();
    long distinctValueCount = 0;
    long totalValueCount = 0;
    long missingCasesCount = 0;
    for (TwoTuple<String,Long> tuple : IteratorUtil.toIterable(distributionStream.iterator())) {
      if (tuple.getKey() == null) {
        missingCasesCount = tuple.getValue();
        continue;
      }
      HistogramBin bin = new HistogramBinImpl();
      bin.setBinStart(tuple.getKey());
      bin.setBinEnd(tuple.getKey());
      bin.setBinLabel(tuple.getKey());
      bin.setValue(tuple.getValue());
      distinctValueCount++;
      totalValueCount += tuple.getValue();
      bins.add(bin);
    }
    HistogramStats stats = new HistogramStatsImpl();
    // FIXME: int casts ok here?
    stats.setSubsetSize(subsetEntityCount);
    stats.setNumMissingCases((int)missingCasesCount);
    stats.setNumDistinctValues((int)distinctValueCount);
    stats.setNumVarValues((int)totalValueCount);
    stats.setNumDistinctEntityRecords((int)(subsetEntityCount - missingCasesCount));
    return new DistributionResult(bins, stats);
  }
}
