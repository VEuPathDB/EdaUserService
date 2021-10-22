package org.veupathdb.service.eda.us.model;

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
import org.veupathdb.service.eda.generated.model.AnalysisProvenance;
import org.veupathdb.service.eda.generated.model.AnalysisProvenanceImpl;
import org.veupathdb.service.eda.us.Utils;
import org.veupathdb.service.eda.us.model.AccountDbData.AccountDataPair;

import static org.veupathdb.service.eda.us.Utils.checkMaxSize;
import static org.veupathdb.service.eda.us.Utils.checkNonEmpty;

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

  public AnalysisDetailWithUser(long ownerId, AnalysisDetailWithUser source, AccountDataPair provenanceOwner) {
    setInitializationFields(ownerId);
    setBaseFields(source);
    setDescriptor(source.getDescriptor());
    setIsPublic(false);
    setProvenance(createProvenance(source, provenanceOwner));
  }

  private static AnalysisProvenance createProvenance(AnalysisDetailWithUser source, AccountDataPair provenanceOwner) {
    AnalysisProvenance p = new AnalysisProvenanceImpl();
    p.setAnalysisId(source.getAnalysisId());
    p.setAnalysisName(source.getDisplayName());
    p.setOwnerId(source.getUserId());
    p.setOwnerName(provenanceOwner.getName());
    p.setOwnerOrganization(provenanceOwner.getOrganization());
    p.setCreationDate(source.getCreationTime());
    p.setModificationDate(source.getModificationTime());
    p.setWasPublic(source.getIsPublic());
    return p;
  }

  private void setInitializationFields(long ownerId) {
    String now = Utils.getCurrentDateTimeString();
    setUserId(ownerId);
    setCreationTime(now);
    setModificationTime(now);
    setAnalysisId(EncryptionUtil.encrypt(getUserId() + now));
  }

  private void setBaseFields(AnalysisBase base) {
    setDisplayName(checkMaxSize(50, "displayName", checkNonEmpty("displayName", base.getDisplayName())));
    setDescription(checkMaxSize(50, "description", base.getDescription()));
    setStudyId(checkMaxSize(50, "studyId", checkNonEmpty("studyId", base.getStudyId())));
    setStudyVersion(checkMaxSize(50, "studyVersion", base.getStudyVersion())); // TODO: will eventually need to be non-empty
    setApiVersion(checkMaxSize(50, "apiVersion", base.getApiVersion()));       // TODO: will eventually need to be non-empty
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
