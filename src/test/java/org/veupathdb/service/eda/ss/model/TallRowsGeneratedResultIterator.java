package org.veupathdb.service.eda.ss.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.MapBuilder;
<<<<<<< HEAD
import org.veupathdb.service.eda.ss.model.Variable.VariableType;
=======
>>>>>>> template/master

import static org.veupathdb.service.eda.ss.model.RdbmsColumnNames.TT_VARIABLE_ID_COL_NAME;

public class TallRowsGeneratedResultIterator implements Iterator<Map<String,String>> {

  private static final Logger LOG = LogManager.getLogger(TallRowsGeneratedResultIterator.class);

  private static class Record extends ArrayList<Map<String, String>>{}

  private final String _entityPkColName;
  private final Supplier<Record> _baseRecordSupplier;
  private final int _requestedRecordCount;

  private int _deliveredRecordCount;
  private Record _currentRecord;
  private int _nextVariableIndex;

  public TallRowsGeneratedResultIterator(Entity entity, int requestedRecordCount, boolean cacheBaseRecord) {
    // initialize final vars
    _entityPkColName = entity.getPKColName();
    _baseRecordSupplier = getBaseRecordSupplier(entity, cacheBaseRecord);
    _requestedRecordCount = requestedRecordCount;
    // initialize changing vars for first call to iterator methods
    _deliveredRecordCount = 0;
    _currentRecord = getNextRecord();
    _nextVariableIndex = 0;
  }

  private static Supplier<Record> getBaseRecordSupplier(Entity entity, boolean cacheBaseRecord) {
    if (cacheBaseRecord) {
      // create a single base record and return it every time
      Record cachedBaseRecord = createBaseRecord(entity);
      return () -> cachedBaseRecord;
    }
    // return a newly created base record every time
    return () -> createBaseRecord(entity);
  }

  private Record getNextRecord() {
    Record baseRecord = _baseRecordSupplier.get();
    Record newRecord = new Record();
    baseRecord.stream().forEach(baseRow -> {
      Map<String,String> row = new HashMap<>(baseRow);
      row.put(_entityPkColName, "record" + _deliveredRecordCount);
      newRecord.add(row);
    });
    return newRecord;
  }

  @Override
  public boolean hasNext() {
    return _deliveredRecordCount < _requestedRecordCount;
  }

  @Override
  public Map<String, String> next() {
    Map<String,String> row = _currentRecord.get(_nextVariableIndex);
    _nextVariableIndex++;
    if (_nextVariableIndex == _currentRecord.size()) {
      // reached the end of this record
      _deliveredRecordCount++;
      _currentRecord = getNextRecord();
      _nextVariableIndex = 0;
    }
    //LOG.info("next() returning row: " + FormatUtil.prettyPrint(row, FormatUtil.Style.SINGLE_LINE));
    return row;
  }

  private static Record createBaseRecord(Entity entity) {

    Map<String,String> idMap = new HashMap<>();
    for (String colName : entity.getAncestorPkColNames()) {
      idMap.put(colName, "someAncestorId");
    }

    if (entity.getVariables().isEmpty())
      throw new RuntimeException("Test entity must contain variables.");

    Record record = new Record();
    for (Variable var : entity.getVariables()) {
      record.add(new MapBuilder<String,String>()
        .putAll(idMap)
        .put(TT_VARIABLE_ID_COL_NAME, var.getId())
        .put(RdbmsColumnNames.VARIABLE_VALUE_COL_NAME, getDefaultVarValue(var.getType()))
        .toMap());
    }

    //LOG.info("Created the following sample record: " + new JSONArray(record).toString(2));
    return record;
  }

  private static String getDefaultVarValue(VariableType type) {
    return switch(type) {
      case DATE -> FormatUtil.formatDateTimeNoTimezone(new Date(1613328166541L));
      case NUMBER -> String.valueOf(123.4567);
      case STRING -> "some fun string";
      case LONGITUDE -> String.valueOf(160.00000);
    };
  }
}
