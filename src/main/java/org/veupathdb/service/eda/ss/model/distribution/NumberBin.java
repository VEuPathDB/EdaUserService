package org.veupathdb.service.eda.ss.model.distribution;

import org.veupathdb.service.eda.generated.model.HistogramBin;
import org.veupathdb.service.eda.generated.model.HistogramBinImpl;

public class NumberBin implements AbstractBinDistribution.Bin<Double> {

  public final Double _start;
  public final Double _end;
  public final boolean _endEqualsMax;

  private long _binCount = 0;

  public NumberBin(Double start, Double end, boolean endEqualsMax) {
    _start = start;
    _end = end;
    _endEqualsMax = endEqualsMax;
  }

  @Override
  public boolean startsAfter(Double value) {
    return (value < _start);
  }

  @Override
  public boolean accept(Double value, Long count) {
    // start inclusive, end exclusive, unless end == max, then inclusive
    if (value >= _start &&
        (value < _end || (_endEqualsMax && value == _end))) {
      _binCount += count;
      return true;
    }
    return false;
  }

  @Override
  public HistogramBin toHistogramBin() {
    HistogramBin bin = new HistogramBinImpl();
    bin.setBinStart(String.valueOf(_start));
    bin.setBinEnd(String.valueOf(_end));
    bin.setBinLabel("[" + bin.getBinStart() + "," + bin.getBinEnd() + ")");
    bin.setValue(_binCount);
    return bin;
  }
}
