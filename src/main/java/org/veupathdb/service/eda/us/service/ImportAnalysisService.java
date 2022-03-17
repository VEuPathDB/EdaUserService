package org.veupathdb.service.eda.us.service;

import java.util.Optional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.FormatUtil;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.service.eda.generated.model.AnalysisListPostResponse;
import org.veupathdb.service.eda.generated.model.SingleAnalysisPublicInfo;
import org.veupathdb.service.eda.generated.model.SingleAnalysisPublicInfoImpl;
import org.veupathdb.service.eda.generated.resources.ImportAnalysisProjectId;
import org.veupathdb.service.eda.us.Utils;
import org.veupathdb.service.eda.us.model.AccountDbData;
import org.veupathdb.service.eda.us.model.AnalysisDetailWithUser;
import org.veupathdb.service.eda.us.model.IdGenerator;
import org.veupathdb.service.eda.us.model.UserDataFactory;

@Authenticated(allowGuests = true)
public class ImportAnalysisService implements ImportAnalysisProjectId {

  @Context
  private ContainerRequest _request;

  @Override
  public GetImportAnalysisByProjectIdAndAnalysisIdResponse getImportAnalysisByProjectIdAndAnalysisId(String projectId, String analysisId) {
    return GetImportAnalysisByProjectIdAndAnalysisIdResponse.respond200WithApplicationJson(importAnalysis(projectId, analysisId, Optional.empty(), _request));
  }

  @Override
  public GetImportAnalysisInfoByProjectIdAndAnalysisIdResponse getImportAnalysisInfoByProjectIdAndAnalysisId(String projectId, String analysisId) {
    AnalysisDetailWithUser analysis = new UserDataFactory(projectId).getAnalysisById(analysisId);
    SingleAnalysisPublicInfo info = new SingleAnalysisPublicInfoImpl();
    info.setStudyId(analysis.getStudyId());
    return GetImportAnalysisInfoByProjectIdAndAnalysisIdResponse.respond200WithApplicationJson(info);
  }

  public static AnalysisListPostResponse importAnalysis(String projectId, String analysisId, Optional<String> userIdOpt, ContainerRequest request) {

    // create data factory (validates projectId)
    UserDataFactory dataFactory = new UserDataFactory(projectId);

    // look up analysis by ID
    AnalysisDetailWithUser oldAnalysis = dataFactory.getAnalysisById(analysisId);

    // if provided, verify URL's userId and analysisId match
    long userId = userIdOpt.map(userIdStr -> {
        long verifiedId = FormatUtil.isLong(userIdStr) ? Long.valueOf(userIdStr) : Utils.doThrow(new NotFoundException());
        Utils.verifyOwnership(verifiedId, oldAnalysis);
        return verifiedId;
    }).orElse(oldAnalysis.getUserId());

    // make a copy of the analysis, assign a new owner, check display name (must be unique) and insert
    User newOwner = Utils.getActiveUser(request);
    dataFactory.addUserIfAbsent(newOwner);
    AccountDbData.AccountDataPair provenanceOwner = new AccountDbData().getUserDataById(userId);
    AnalysisDetailWithUser newAnalysis = new AnalysisDetailWithUser(
        IdGenerator.getNextAnalysisId(dataFactory), newOwner.getUserID(), oldAnalysis, provenanceOwner);

    dataFactory.insertAnalysis(newAnalysis);
    return newAnalysis.getIdObject();

  }
}
