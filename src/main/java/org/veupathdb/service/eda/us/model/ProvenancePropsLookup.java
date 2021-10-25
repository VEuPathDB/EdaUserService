package org.veupathdb.service.eda.us.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import org.veupathdb.service.eda.generated.model.AnalysisProvenance;
import org.veupathdb.service.eda.generated.model.AnalysisSummary;
import org.veupathdb.service.eda.generated.model.CurrentProvenanceProps;
import org.veupathdb.service.eda.generated.model.CurrentProvenancePropsImpl;

public class ProvenancePropsLookup {

  private static CurrentProvenanceProps toCurrentProps(String modTime) {
    CurrentProvenanceProps currentState = new CurrentProvenancePropsImpl();
    currentState.setIsDeleted(modTime == null);
    currentState.setModificationTime(modTime);
    return currentState;
  }

  public static void assignCurrentProvenanceProps(List<AnalysisSummary> summaries) {
    Map<String, CurrentProvenanceProps> idToCurrentPropsCache = new HashMap<>();
    // add all mod dates in the current list to save lookups already done
    summaries.stream().forEach(a ->
        idToCurrentPropsCache.put(a.getAnalysisId(), toCurrentProps(a.getModificationTime())));
    summaries.stream().forEach(a -> {
      AnalysisProvenance prov = a.getProvenance();
      if (prov != null) {
        String parentId = prov.getOnImport().getAnalysisId();
        CurrentProvenanceProps currentProps = idToCurrentPropsCache.get(parentId);
        if (currentProps == null) {
          // still need to look up
          try {
            // try to find parent analysis (may have been deleted)
            currentProps = toCurrentProps(UserDataFactory.getAnalysisById(parentId).getModificationTime());
          }
          catch (NotFoundException e) {
            // parent has been deleted
            currentProps = toCurrentProps(null);
          }
          idToCurrentPropsCache.put(parentId, currentProps);
        }
        prov.setCurrent(currentProps);
      }
    });
  }
}