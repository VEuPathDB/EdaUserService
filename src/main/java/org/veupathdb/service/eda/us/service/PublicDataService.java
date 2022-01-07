package org.veupathdb.service.eda.us.service;

import java.util.List;
import org.veupathdb.service.eda.generated.model.AnalysisSummaryWithUser;
import org.veupathdb.service.eda.generated.resources.PublicAnalysesProjectId;
import org.veupathdb.service.eda.us.model.AccountDbData;
import org.veupathdb.service.eda.us.model.ProvenancePropsLookup;
import org.veupathdb.service.eda.us.model.UserDataFactory;

public class PublicDataService implements PublicAnalysesProjectId {

  @Override
  public GetPublicAnalysesByProjectIdResponse getPublicAnalysesByProjectId(String projectId) {
    UserDataFactory dataFactory = new UserDataFactory(projectId);
    List<AnalysisSummaryWithUser> publicAnalyses = dataFactory.getPublicAnalyses();
    ProvenancePropsLookup.assignCurrentProvenanceProps(dataFactory, publicAnalyses);
    return GetPublicAnalysesByProjectIdResponse.respond200WithApplicationJson(
        new AccountDbData().populateOwnerData(publicAnalyses));
  }
}
