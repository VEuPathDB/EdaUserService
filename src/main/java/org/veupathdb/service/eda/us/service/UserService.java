package org.veupathdb.service.eda.us.service;

import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import org.gusdb.fgputil.FormatUtil;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.service.eda.generated.model.AnalysisDetail;
import org.veupathdb.service.eda.generated.model.AnalysisListPatchRequest;
import org.veupathdb.service.eda.generated.model.AnalysisListPostRequest;
import org.veupathdb.service.eda.generated.model.AnalysisSummary;
import org.veupathdb.service.eda.generated.model.SingleAnalyisPatchRequest;
import org.veupathdb.service.eda.generated.resources.UsersUserId;
import org.veupathdb.service.eda.us.Utils;
import org.veupathdb.service.eda.us.model.AnalysisDetailWithUser;
import org.veupathdb.service.eda.us.model.UserDataFactory;

@Authenticated(allowGuests = true)
public class UserService implements UsersUserId {

  @Context
  private Request _request;

  @Override
  public GetUsersPreferencesByUserIdResponse getUsersPreferencesByUserId(String userId) {
    User user = Utils.getAuthorizedUser(_request, userId);
    String prefs = UserDataFactory.readPreferences(user.getUserID());
    return GetUsersPreferencesByUserIdResponse.respond200WithApplicationJson(prefs);
  }

  @Override
  public PutUsersPreferencesByUserIdResponse putUsersPreferencesByUserId(String userId, String entity) {
    User user = Utils.getAuthorizedUser(_request, userId);
    UserDataFactory.addUserIfAbsent(user);
    UserDataFactory.writePreferences(user.getUserID(), entity);
    return PutUsersPreferencesByUserIdResponse.respond202();
  }

  @Override
  public GetUsersAnalysesByUserIdResponse getUsersAnalysesByUserId(String userId) {
    List<AnalysisSummary> summaries = UserDataFactory.getAnalysisSummaries(Utils.getAuthorizedUser(_request, userId).getUserID());
    return GetUsersAnalysesByUserIdResponse.respond200WithApplicationJson(summaries);
  }

  @Override
  public PostUsersAnalysesByUserIdResponse postUsersAnalysesByUserId(String userId, AnalysisListPostRequest entity) {
    User user = Utils.getAuthorizedUser(_request, userId);
    UserDataFactory.addUserIfAbsent(user);
    AnalysisDetailWithUser newAnalysis = new AnalysisDetailWithUser(user.getUserID(), entity);
    UserDataFactory.insertAnalysis(newAnalysis);
    return PostUsersAnalysesByUserIdResponse.respond200WithApplicationJson(newAnalysis.getIdObject());
  }

  @Override
  public GetUsersAnalysesByUserIdAndAnalysisIdResponse getUsersAnalysesByUserIdAndAnalysisId(String userId, String analysisId) {
    User user = Utils.getAuthorizedUser(_request, userId);
    AnalysisDetailWithUser analysis = UserDataFactory.getAnalysisById(analysisId);
    Utils.verifyOwnership(user.getUserID(), analysis);
    return GetUsersAnalysesByUserIdAndAnalysisIdResponse.respond200WithApplicationJson(analysis);
  }

  @Override
  public PatchUsersAnalysesByUserIdResponse patchUsersAnalysesByUserId(String userId, AnalysisListPatchRequest entity) {
    User user = Utils.getAuthorizedUser(_request, userId);
    List<String> idsToDelete = entity.getAnalysisIdsToDelete();
    if (idsToDelete != null && !idsToDelete.isEmpty()) {
      try {
        String[] idArray = idsToDelete.toArray(new String[0]);
        Utils.verifyOwnership(user.getUserID(), idArray);
        UserDataFactory.deleteAnalyses(idArray);
      }
      catch (NotFoundException nfe) {
        // validateOwnership throws not found if ID does not exist; convert to 400
        throw new BadRequestException(nfe.getMessage());
      }
    }
    return PatchUsersAnalysesByUserIdResponse.respond202();
  }

  @Override
  public PatchUsersAnalysesByUserIdAndAnalysisIdResponse patchUsersAnalysesByUserIdAndAnalysisId(String userId, String analysisId, SingleAnalyisPatchRequest entity) {
    User user = Utils.getAuthorizedUser(_request, userId);
    AnalysisDetailWithUser analysis = UserDataFactory.getAnalysisById(analysisId);
    Utils.verifyOwnership(user.getUserID(), analysis);
    editAnalysis(analysis, entity);
    UserDataFactory.updateAnalysis(analysis);
    return PatchUsersAnalysesByUserIdAndAnalysisIdResponse.respond202();
  }

  private static void editAnalysis(AnalysisDetail analysis, SingleAnalyisPatchRequest entity) {
    boolean changeMade = false;
    // FIXME: need to box boolean primitive in generated code; this is broken!
    if ((Boolean)entity.getIsPublic() != null) {
      changeMade = true; analysis.setIsPublic(entity.getIsPublic());
    }
    if (entity.getDisplayName() != null) {
      changeMade = true; analysis.setDisplayName(entity.getDisplayName());
    }
    if (entity.getDescription() != null) {
      changeMade = true; analysis.setDescription(entity.getDescription());
    }
    if (entity.getDescriptor() != null) {
      changeMade = true; analysis.setDescriptor(entity.getDescriptor());
    }
    if (changeMade) {
      analysis.setModificationTime(Utils.getCurrentDateTimeString());
    }
  }

  @Override
  public DeleteUsersAnalysesByUserIdAndAnalysisIdResponse deleteUsersAnalysesByUserIdAndAnalysisId(String userId, String analysisId) {
    User user = Utils.getAuthorizedUser(_request, userId);
    Utils.verifyOwnership(user.getUserID(), analysisId);
    UserDataFactory.deleteAnalyses(analysisId);
    return DeleteUsersAnalysesByUserIdAndAnalysisIdResponse.respond202();
  }

  @Override
  public PostUsersAnalysesCopyByUserIdAndAnalysisIdResponse postUsersAnalysesCopyByUserIdAndAnalysisId(String userIdStr, String analysisId) {

    // verify URL's userId and analysisId exist and match
    long userId = FormatUtil.isLong(userIdStr) ? Long.valueOf(userIdStr) : Utils.doThrow(new NotFoundException());
    AnalysisDetailWithUser oldAnalysis = UserDataFactory.getAnalysisById(analysisId);
    Utils.verifyOwnership(userId, oldAnalysis);

    // make a copy of the analysis, assign a new owner, and insert
    User newOwner = Utils.getActiveUser(_request);
    UserDataFactory.addUserIfAbsent(newOwner);
    AnalysisDetailWithUser newAnalysis = new AnalysisDetailWithUser(newOwner.getUserID(), oldAnalysis);
    UserDataFactory.insertAnalysis(newAnalysis);

    return PostUsersAnalysesCopyByUserIdAndAnalysisIdResponse.respond200WithApplicationJson(newAnalysis.getIdObject());
  }
}
