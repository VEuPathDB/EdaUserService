package org.veupathdb.service.eda.us.service;

import java.util.Optional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import org.gusdb.fgputil.FormatUtil;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.service.eda.generated.model.AnalysisListPostResponse;
import org.veupathdb.service.eda.generated.model.SingleAnalysisPublicInfo;
import org.veupathdb.service.eda.generated.model.SingleAnalysisPublicInfoImpl;
import org.veupathdb.service.eda.generated.resources.ImportAnalysis;
import org.veupathdb.service.eda.us.Utils;
import org.veupathdb.service.eda.us.model.AccountDbData;
import org.veupathdb.service.eda.us.model.AnalysisDetailWithUser;
import org.veupathdb.service.eda.us.model.UserDataFactory;

@Authenticated(allowGuests = true)
public class ImportAnalysisService implements ImportAnalysis {

  @Context
  private Request _request;

  @Override
  public GetImportAnalysisByAnalysisIdResponse getImportAnalysisByAnalysisId(String analysisId) {
    return GetImportAnalysisByAnalysisIdResponse.respond200WithApplicationJson(importAnalysis(analysisId, Optional.empty(), _request));
  }

  @Override
  public GetImportAnalysisInfoByAnalysisIdResponse getImportAnalysisInfoByAnalysisId(String analysisId) {
    AnalysisDetailWithUser analysis = UserDataFactory.getAnalysisById(analysisId);
    SingleAnalysisPublicInfo info = new SingleAnalysisPublicInfoImpl();
    info.setStudyId(analysis.getStudyId());
    return GetImportAnalysisInfoByAnalysisIdResponse.respond200WithApplicationJson(info);
  }

  public static AnalysisListPostResponse importAnalysis(String analysisId, Optional<String> userIdOpt, Request request) {

    // look up analysis by ID
    AnalysisDetailWithUser oldAnalysis = UserDataFactory.getAnalysisById(analysisId);

    // if provided, verify URL's userId and analysisId match
    long userId = userIdOpt.map(userIdStr -> {
        long verifiedId = FormatUtil.isLong(userIdStr) ? Long.valueOf(userIdStr) : Utils.doThrow(new NotFoundException());
        Utils.verifyOwnership(verifiedId, oldAnalysis);
        return verifiedId;
    }).orElse(oldAnalysis.getUserId());

    // make a copy of the analysis, assign a new owner, check display name (must be unique) and insert
    User newOwner = Utils.getActiveUser(request);
    UserDataFactory.addUserIfAbsent(newOwner);
    AccountDbData.AccountDataPair provenanceOwner = new AccountDbData().getUserDataById(userId);
    AnalysisDetailWithUser newAnalysis = new AnalysisDetailWithUser(newOwner.getUserID(), oldAnalysis, provenanceOwner);

    UserDataFactory.insertAnalysis(newAnalysis);
    return newAnalysis.getIdObject();

  }
}
