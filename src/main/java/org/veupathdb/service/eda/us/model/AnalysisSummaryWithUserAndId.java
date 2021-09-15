package org.veupathdb.service.eda.us.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.veupathdb.service.eda.generated.model.AnalysisSummaryWithUserImpl;

public class AnalysisSummaryWithUserAndId extends AnalysisSummaryWithUserImpl {

  private final long _userId;

  public AnalysisSummaryWithUserAndId(long userId) {
    _userId = userId;
    setUserName("User_" + userId);
    setUserOrganization("");
  }

  @JsonIgnore
  public long getUserId() {
    return _userId;
  }
}
