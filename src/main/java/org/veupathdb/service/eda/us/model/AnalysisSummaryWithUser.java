package org.veupathdb.service.eda.us.model;

import org.veupathdb.service.eda.generated.model.AnalysisSummaryWithUserImpl;

public class AnalysisSummaryWithUser extends AnalysisSummaryWithUserImpl {

  public AnalysisSummaryWithUser(long userId) {
    setUserId(userId);
    setUserName("User_" + userId);
    setUserOrganization("");
  }

}
