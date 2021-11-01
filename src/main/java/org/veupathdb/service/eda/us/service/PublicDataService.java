package org.veupathdb.service.eda.us.service;

import java.util.List;
import org.veupathdb.service.eda.generated.model.AnalysisSummaryWithUser;
import org.veupathdb.service.eda.generated.resources.Public;
import org.veupathdb.service.eda.us.model.AccountDbData;
import org.veupathdb.service.eda.us.model.ProvenancePropsLookup;
import org.veupathdb.service.eda.us.model.UserDataFactory;

public class PublicDataService implements Public {

  @Override
  public GetPublicAnalysesResponse getPublicAnalyses() {
    List<AnalysisSummaryWithUser> publicAnalyses = UserDataFactory.getPublicAnalyses();
    ProvenancePropsLookup.assignCurrentProvenanceProps(publicAnalyses);
    return GetPublicAnalysesResponse.respond200WithApplicationJson(
        new AccountDbData().populateOwnerData(publicAnalyses));
  }

}
