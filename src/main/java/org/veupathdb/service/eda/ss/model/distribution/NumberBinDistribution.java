package org.veupathdb.service.eda.ss.model.distribution;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.sql.DataSource;
import javax.ws.rs.BadRequestException;
import org.veupathdb.service.eda.generated.model.BinSpecWithRange;
import org.veupathdb.service.eda.generated.model.HistogramStats;
import org.veupathdb.service.eda.generated.model.HistogramStatsImpl;
import org.veupathdb.service.eda.generated.model.ValueSpec;
import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.NumberVariable;
import org.veupathdb.service.eda.ss.model.Study;
import org.veupathdb.service.eda.ss.model.filter.Filter;

public class NumberBinDistribution extends AbstractBinDistribution<NumberVariable, Double, NumberBin> {

  private final double _binWidth;

  public NumberBinDistribution(DataSource ds, Study study, Entity targetEntity, NumberVariable var,
                               List<Filter> filters, ValueSpec valueSpec, BinSpecWithRange binSpec) {
    super(ds, study, targetEntity, var, filters, valueSpec, binSpec.getDisplayRangeMin(), binSpec.getDisplayRangeMax(), binSpec);
    _binWidth = binSpec.getBinWidth().doubleValue();
  }

  @Override
  protected Double getTypedObject(String objectName, Object value, ValueSource source) {
    Supplier<RuntimeException> exSupplier = () -> switch(source) {
      case CONFIG -> new BadRequestException(objectName + " must be a number value.");
      case DB -> new RuntimeException("Value in column " + objectName + " is not a number.");
    };
    if (value instanceof Number) {
      return ((Number)value).doubleValue();
    }
    if (value instanceof String) {
      try {
        return Double.parseDouble((String)value);
      }
      catch (NumberFormatException e) {
        throw exSupplier.get();
      }
    }
    throw exSupplier.get();
  }

  @Override
  protected StatsCollector getStatsCollector() {
    return new StatsCollector() {

      private Double _subsetMin;
      private Double _subsetMax;
      private long _numValues = 0;
      private long _numDistinctValues = 0;
      private double _sumOfValues = 0;

      @Override
      void accept(Double value, Long count) {
        if (_subsetMin == null) _subsetMin = value;
        _subsetMax = value;
        _numValues += count;
        _numDistinctValues++;
        _sumOfValues += (count * value);
      }

      @Override
      HistogramStats toHistogramStats(long subsetEntityCount, long missingCasesCount) {
        HistogramStats stats = new HistogramStatsImpl();
        stats.setSubsetMin(_subsetMin);
        stats.setSubsetMax(_subsetMax);
        stats.setSubsetMean(_sumOfValues / _numValues);
        // FIXME: int casts ok here?
        stats.setSubsetSize((int)subsetEntityCount);
        stats.setNumVarValues((int)_numValues);
        stats.setNumDistinctValues((int)_numDistinctValues);
        stats.setNumDistinctEntityRecords((int)(subsetEntityCount - missingCasesCount));
        stats.setNumMissingCases((int)missingCasesCount);
        return stats;
      }
    };
  }

  @Override
  protected NumberBin getFirstBin() {
    return getNextBin(new NumberBin(null, _displayMin, false)).orElseThrow();
  }

  @Override
  protected Optional<NumberBin> getNextBin(NumberBin currentBin) {
    return _displayMax <= currentBin._end ? Optional.empty() :
        Optional.of(new NumberBin(currentBin._end, currentBin._end + _binWidth, _displayMax == currentBin._end + _binWidth));
  }

}
