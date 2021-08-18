package org.veupathdb.service.eda.ss.model.distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.iterator.IteratorUtil;
import org.veupathdb.service.eda.generated.model.BinSpecWithRange;
import org.veupathdb.service.eda.generated.model.HistogramBin;
import org.veupathdb.service.eda.generated.model.HistogramStats;
import org.veupathdb.service.eda.generated.model.ValueSpec;
import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.Study;
import org.veupathdb.service.eda.ss.model.VariableWithValues;
import org.veupathdb.service.eda.ss.model.distribution.AbstractBinDistribution.Bin;
import org.veupathdb.service.eda.ss.model.filter.Filter;

public abstract class AbstractBinDistribution<T extends VariableWithValues, S, R extends Bin<S>> extends AbstractDistribution<T> {

  protected enum ValueSource { CONFIG, DB }

  public interface Bin<T> {
    boolean startsAfter(T value);
    boolean accept(T value, Long count);
    HistogramBin toHistogramBin();
  }

  protected abstract class StatsCollector {
    abstract void accept(S value, Long count);
    abstract HistogramStats toHistogramStats(long subsetEntityCount, long missingCasesCount);
  }

  protected final S _displayMin;
  protected final S _displayMax;

  protected abstract S getTypedObject(String objectName, Object value, ValueSource source);
  protected abstract StatsCollector getStatsCollector();
  protected abstract R getFirstBin();
  protected abstract Optional<R> getNextBin(R currentBin);

  public AbstractBinDistribution(DataSource ds, Study study, Entity targetEntity,
                                 T variable, List<Filter> filters, ValueSpec valueSpec,
                                 Object displayMin, Object displayMax, BinSpecWithRange binSpec) {
    super(ds, study, targetEntity, variable, filters, valueSpec);
    _displayMin = adjustMin(getTypedObject("displayMin", displayMin, ValueSource.CONFIG), binSpec);
    _displayMax = adjustMax(getTypedObject("displayMax", displayMax, ValueSource.CONFIG), binSpec);
  }

  protected S adjustMin(S displayMin, BinSpecWithRange binSpec) {
    return displayMin; // no adjustment by default
  }

  protected S adjustMax(S displayMax, BinSpecWithRange binSpec) {
    return displayMax; // no adjustment by default
  }

  @Override
  protected DistributionResult processDistributionStream(Stream<TwoTuple<String, Long>> distributionStream, int subsetEntityCount) {
    StatsCollector stats = getStatsCollector();
    long missingCasesCount = 0;
    R currentBin = getFirstBin();
    boolean beforeFirstBin = true;
    boolean afterLastBin = false;
    List<HistogramBin> bins = new ArrayList<>();
    for (TwoTuple<String,Long> tuple : IteratorUtil.toIterable(distributionStream.iterator())) {

      // handle missing cases (not added to stats)
      if (tuple.getKey() == null) {
        missingCasesCount = tuple.getValue();
        continue;
      }

      // convert value to comparable object
      S value = getTypedObject("value", tuple.getKey(), ValueSource.DB);

      // use all values for stats, even if they are outside the specified range
      stats.accept(value, tuple.getValue());

      // skip over values less than the specified range (do not fit in any bin)
      if (beforeFirstBin && currentBin.startsAfter(value)) {
        continue;
      }
      beforeFirstBin = false;

      // add values to bins until bins end or values end
      while (!afterLastBin && !currentBin.accept(value, tuple.getValue())) {
        // value not accepted; move to the next bin if present
        Optional<R> nextBin = getNextBin(currentBin);
        if (nextBin.isPresent()) {
          bins.add(currentBin.toHistogramBin());
          currentBin = nextBin.get();
        }
        else {
          afterLastBin = true;
        }
      }
    }

    // will always have a leftover bin; add it
    bins.add(currentBin.toHistogramBin());

    // may have ended values before the last bin was fetched
    if (!afterLastBin) {
      Optional<R> nextBin = getNextBin(currentBin);
      while (nextBin.isPresent()) {
        bins.add(nextBin.get().toHistogramBin());
        nextBin = getNextBin(nextBin.get());
      }
    }

    // return result
    return new DistributionResult(bins, stats.toHistogramStats(subsetEntityCount, missingCasesCount));
  }

}
