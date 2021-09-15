package org.veupathdb.service.eda.us.service;

import org.veupathdb.service.eda.generated.resources.Public;
import org.veupathdb.service.eda.us.model.AccountDbData;
import org.veupathdb.service.eda.us.model.UserDataFactory;

public class PublicDataService implements Public {

  @Override
  public GetPublicAnalysesResponse getPublicAnalyses() {
    return GetPublicAnalysesResponse.respond200WithApplicationJson(
        new AccountDbData().populateOwnerData(UserDataFactory.getPublicAnalyses()));
  }

}
