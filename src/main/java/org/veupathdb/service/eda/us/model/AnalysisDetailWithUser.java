package org.veupathdb.service.eda.us.model;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.functional.Functions;
import org.veupathdb.service.eda.generated.model.AnalysisBase;
import org.veupathdb.service.eda.generated.model.AnalysisDescriptor;
import org.veupathdb.service.eda.generated.model.AnalysisDetailImpl;
import org.veupathdb.service.eda.generated.model.AnalysisListPostRequest;
import org.veupathdb.service.eda.generated.model.AnalysisListPostResponse;
import org.veupathdb.service.eda.generated.model.AnalysisListPostResponseImpl;
import org.veupathdb.service.eda.us.Utils;

/**
 * Non-API analysis data container; used to pass information about a single
 * analysis (detail) back and forth between the service and data factory
 */
public class AnalysisDetailWithUser extends AnalysisDetailImpl {

  private long _userId;

  public AnalysisDetailWithUser(ResultSet rs) throws SQLException {
    UserDataFactory.populateDetailFields(this, rs);
  }

  public AnalysisDetailWithUser(long ownerId, AnalysisListPostRequest request) {
    setInitializationFields(ownerId);
    setBaseFields(request);
    setDescriptor(request.getDescriptor());
    setIsPublic(request.getIsPublic());
  }

  public AnalysisDetailWithUser(long ownerId, AnalysisDetailWithUser source) {
    setInitializationFields(ownerId);
    setBaseFields(source);
    setDescriptor(source.getDescriptor());
    setIsPublic(false);
  }

  private void setInitializationFields(long ownerId) {
    String now = Utils.getCurrentDateTimeString();
    setUserId(ownerId);
    setCreationTime(now);
    setModificationTime(now);
    setAnalysisId(EncryptionUtil.encrypt(getUserId() + now));
  }

  private void setBaseFields(AnalysisBase base) {
    setDisplayName(base.getDisplayName());
    setDescription(base.getDescription());
    setStudyId(base.getStudyId());
    setStudyVersion(base.getStudyVersion());
    setApiVersion(base.getApiVersion());
  }

  public void setDescriptor(AnalysisDescriptor descriptor) {
    super.setDescriptor(descriptor);
    setNumFilters((long)descriptor.getSubset().getDescriptor().size());
    setNumComputations((long)descriptor.getComputations().size());
    setNumVisualizations((long)Functions.reduce(descriptor.getComputations(),
        (count, next) -> count + next.getVisualizations().size(), 0));
  }

  @JsonIgnore
  public long getUserId() {
    return _userId;
  }

  public void setUserId(long userId) {
    _userId = userId;
  }

  @JsonIgnore
  public AnalysisListPostResponse getIdObject() {
    AnalysisListPostResponse idObj = new AnalysisListPostResponseImpl();
    idObj.setAnalysisId(getAnalysisId());
    return idObj;
  }
}
