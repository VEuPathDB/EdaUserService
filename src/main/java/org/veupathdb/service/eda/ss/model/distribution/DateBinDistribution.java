package org.veupathdb.service.eda.ss.model.distribution;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import javax.ws.rs.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.veupathdb.service.eda.generated.model.BinSpecWithRange;
import org.veupathdb.service.eda.generated.model.BinUnits;
import org.veupathdb.service.eda.generated.model.HistogramStats;
import org.veupathdb.service.eda.generated.model.HistogramStatsImpl;
import org.veupathdb.service.eda.generated.model.ValueSpec;
import org.veupathdb.service.eda.ss.model.DateVariable;
import org.veupathdb.service.eda.ss.model.Entity;
import org.veupathdb.service.eda.ss.model.Study;
import org.veupathdb.service.eda.ss.model.filter.Filter;
import org.veupathdb.service.eda.ss.service.RequestBundle;

public class DateBinDistribution extends AbstractBinDistribution<DateVariable, LocalDateTime, DateBin> {

  private static final Logger LOG = LogManager.getLogger(DateBinDistribution.class);

  private final ChronoUnit _binUnits;
  private final int _binSize;

  public DateBinDistribution(DataSource ds, Study study, Entity targetEntity, DateVariable var,
                             List<Filter> filters, ValueSpec valueSpec, BinSpecWithRange binSpec) {
    super(ds, study, targetEntity, var, filters, valueSpec, binSpec.getDisplayRangeMin(), binSpec.getDisplayRangeMax(), binSpec);
    _binUnits = calculateChronoUnit(binSpec.getBinUnits());
    _binSize = binSpec.getBinWidth().intValue();
  }

  private ChronoUnit calculateChronoUnit(BinUnits binUnits) {
    return switch(binUnits) {
      case DAY -> ChronoUnit.DAYS;
      case WEEK -> ChronoUnit.WEEKS;
      case MONTH -> ChronoUnit.MONTHS;
      case YEAR -> ChronoUnit.YEARS;
    };
  }

  @Override
  protected LocalDateTime adjustMin(LocalDateTime displayMin, BinSpecWithRange binSpec) {
    switch(calculateChronoUnit(binSpec.getBinUnits())) {
      case MONTHS:
        // truncate to the beginning of the month
        return LocalDateTime.of(displayMin.getYear(), displayMin.getMonth(), 1, 0, 0);
      case YEARS:
        // truncate to the beginning of the year
        return LocalDateTime.of(displayMin.getYear(), Month.JANUARY, 1, 0, 0);
      default:
        // for days and weeks, truncate to the beginning of the day
        return displayMin.truncatedTo(ChronoUnit.DAYS);
    }
  }

  @Override
  protected LocalDateTime adjustMax(LocalDateTime displayMax, BinSpecWithRange binSpec) {
    // truncate to the day (floor), then add 1 day and subtract 1 second
    //    this way all values on that day fall before the max, but none after
    return displayMax
        .truncatedTo(ChronoUnit.DAYS)
        .plus(1, ChronoUnit.DAYS)
        .minus(1, ChronoUnit.SECONDS);
  }

  @Override
  protected LocalDateTime getTypedObject(String objectName, Object value, ValueSource source) {
    if (value instanceof String) {
      return RequestBundle.parseDate((String)value);
    }
    throw switch(source) {
      case CONFIG -> new BadRequestException(objectName + " must be a date string value.");
      case DB -> new RuntimeException("Converted value in column " + objectName + " is not a valid date string.");
    };
  }

  @Override
  protected StatsCollector getStatsCollector() {
    return new StatsCollector() {

      private LocalDateTime _subsetMin;
      private LocalDateTime _subsetMax;
      private long _numValues = 0;
      private long _numDistinctValues = 0;
      private long _sumOfValues = 0;

      @Override
      void accept(LocalDateTime value, Long count) {
        if (_subsetMin == null) _subsetMin = value;
        _subsetMax = value;
        _numValues += count;
        _numDistinctValues++;
        _sumOfValues += (count * value.toEpochSecond(ZoneOffset.UTC));
      }

      @Override
      HistogramStats toHistogramStats(long subsetEntityCount, long missingCasesCount) {
        HistogramStats stats = new HistogramStatsImpl();
        stats.setSubsetMin(RequestBundle.formatDate(_subsetMin));
        stats.setSubsetMax(RequestBundle.formatDate(_subsetMax));
        stats.setSubsetMean(RequestBundle.formatDate(
            LocalDateTime.ofEpochSecond(_sumOfValues / _numValues, 0, ZoneOffset.UTC)));
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
  protected DateBin getFirstBin() {
    return getNextBin(new DateBin(null, _displayMin, _binUnits, _binSize)).orElseThrow();
  }

  @Override
  protected Optional<DateBin> getNextBin(DateBin currentBin) {
    return _displayMax.isBefore(currentBin._end) ? Optional.empty() :
        Optional.of(new DateBin(currentBin._end, currentBin._end.plus(_binSize, _binUnits), _binUnits, _binSize));
  }

}
