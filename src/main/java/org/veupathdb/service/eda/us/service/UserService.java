package org.veupathdb.service.eda.us.service;

import java.util.List;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import org.glassfish.jersey.server.ContainerRequest;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.service.eda.generated.model.AnalysisDetail;
import org.veupathdb.service.eda.generated.model.AnalysisListPatchRequest;
import org.veupathdb.service.eda.generated.model.AnalysisListPostRequest;
import org.veupathdb.service.eda.generated.model.AnalysisSummary;
import org.veupathdb.service.eda.generated.model.SingleAnalysisPatchRequest;
import org.veupathdb.service.eda.generated.resources.UsersUserId;
import org.veupathdb.service.eda.us.Utils;
import org.veupathdb.service.eda.us.model.AnalysisDetailWithUser;
import org.veupathdb.service.eda.us.model.IdGenerator;
import org.veupathdb.service.eda.us.model.ProvenancePropsLookup;
import org.veupathdb.service.eda.us.model.UserDataFactory;

import static org.veupathdb.service.eda.us.Utils.checkMaxSize;
import static org.veupathdb.service.eda.us.Utils.checkNonEmpty;

@Authenticated(allowGuests = true)
public class UserService implements UsersUserId {

  @Context
  private ContainerRequest _request;

  @Override
  public GetUsersPreferencesByUserIdAndProjectIdResponse getUsersPreferencesByUserIdAndProjectId(String userId, String projectId) {
    UserDataFactory dataFactory = new UserDataFactory(projectId);
    User user = Utils.getAuthorizedUser(_request, userId);
    String prefs = dataFactory.readPreferences(user.getUserID());
    return GetUsersPreferencesByUserIdAndProjectIdResponse.respond200WithApplicationJson(prefs);
  }

  @Override
  public PutUsersPreferencesByUserIdAndProjectIdResponse putUsersPreferencesByUserIdAndProjectId(String userId, String projectId, String entity) {
    UserDataFactory dataFactory = new UserDataFactory(projectId);
    User user = Utils.getAuthorizedUser(_request, userId);
    dataFactory.addUserIfAbsent(user);
    dataFactory.writePreferences(user.getUserID(), entity);
    return PutUsersPreferencesByUserIdAndProjectIdResponse.respond202();
  }

  @Override
  public GetUsersAnalysesByUserIdAndProjectIdResponse getUsersAnalysesByUserIdAndProjectId(String userId, String projectId) {
    UserDataFactory dataFactory = new UserDataFactory(projectId);
    List<AnalysisSummary> summaries = dataFactory.getAnalysisSummaries(Utils.getAuthorizedUser(_request, userId).getUserID());
    ProvenancePropsLookup.assignCurrentProvenanceProps(dataFactory, summaries);
    return GetUsersAnalysesByUserIdAndProjectIdResponse.respond200WithApplicationJson(summaries);
  }

  @Override
  public PostUsersAnalysesByUserIdAndProjectIdResponse postUsersAnalysesByUserIdAndProjectId(String userId, String projectId, AnalysisListPostRequest entity) {
    UserDataFactory dataFactory = new UserDataFactory(projectId);
    User user = Utils.getAuthorizedUser(_request, userId);
    dataFactory.addUserIfAbsent(user);
    AnalysisDetailWithUser newAnalysis = new AnalysisDetailWithUser(
        IdGenerator.getNextAnalysisId(dataFactory), user.getUserID(), entity);
    dataFactory.insertAnalysis(newAnalysis);
    return PostUsersAnalysesByUserIdAndProjectIdResponse.respond200WithApplicationJson(newAnalysis.getIdObject());
  }

  @Override
  public PatchUsersAnalysesByUserIdAndProjectIdResponse patchUsersAnalysesByUserIdAndProjectId(String userId, String projectId, AnalysisListPatchRequest entity) {
    UserDataFactory dataFactory = new UserDataFactory(projectId);
    User user = Utils.getAuthorizedUser(_request, userId);
    performBulkDeletion(dataFactory, user, entity.getAnalysisIdsToDelete());
    performInheritGuestAnalyses(dataFactory, user, entity.getInheritOwnershipFrom());
    return PatchUsersAnalysesByUserIdAndProjectIdResponse.respond202();
  }

  @Override
  public GetUsersAnalysesByUserIdAndProjectIdAndAnalysisIdResponse getUsersAnalysesByUserIdAndProjectIdAndAnalysisId(String userId, String projectId, String analysisId) {
    UserDataFactory dataFactory = new UserDataFactory(projectId);
    User user = Utils.getAuthorizedUser(_request, userId);
    AnalysisDetailWithUser analysis = dataFactory.getAnalysisById(analysisId);
    Utils.verifyOwnership(user.getUserID(), analysis);
    ProvenancePropsLookup.assignCurrentProvenanceProps(dataFactory, List.of(analysis));
    return GetUsersAnalysesByUserIdAndProjectIdAndAnalysisIdResponse.respond200WithApplicationJson(analysis);
  }

  @Override
  public PatchUsersAnalysesByUserIdAndProjectIdAndAnalysisIdResponse patchUsersAnalysesByUserIdAndProjectIdAndAnalysisId(String userId, String projectId, String analysisId, SingleAnalysisPatchRequest entity) {
    UserDataFactory dataFactory = new UserDataFactory(projectId);
    User user = Utils.getAuthorizedUser(_request, userId);
    AnalysisDetailWithUser analysis = dataFactory.getAnalysisById(analysisId);
    Utils.verifyOwnership(user.getUserID(), analysis);
    editAnalysis(user, analysis, entity);
    dataFactory.updateAnalysis(analysis);
    return PatchUsersAnalysesByUserIdAndProjectIdAndAnalysisIdResponse.respond202();
  }

  @Override
  public DeleteUsersAnalysesByUserIdAndProjectIdAndAnalysisIdResponse deleteUsersAnalysesByUserIdAndProjectIdAndAnalysisId(String userId, String projectId, String analysisId) {
    UserDataFactory dataFactory = new UserDataFactory(projectId);
    User user = Utils.getAuthorizedUser(_request, userId);
    Utils.verifyOwnership(dataFactory, user.getUserID(), analysisId);
    dataFactory.deleteAnalyses(analysisId);
    return DeleteUsersAnalysesByUserIdAndProjectIdAndAnalysisIdResponse.respond202();
  }

  @Override
  public PostUsersAnalysesCopyByUserIdAndProjectIdAndAnalysisIdResponse postUsersAnalysesCopyByUserIdAndProjectIdAndAnalysisId(String userId, String projectId, String analysisId) {
    return PostUsersAnalysesCopyByUserIdAndProjectIdAndAnalysisIdResponse.respond200WithApplicationJson(
        ImportAnalysisService.importAnalysis(projectId, analysisId, Optional.of(userId), _request));
  }

  private void performBulkDeletion(UserDataFactory dataFactory, User user, List<String> analysisIdsToDelete) {
    if (analysisIdsToDelete == null || analysisIdsToDelete.isEmpty())
      return;
    try {
      String[] idArray = analysisIdsToDelete.toArray(new String[0]);
      Utils.verifyOwnership(dataFactory, user.getUserID(), idArray);
      dataFactory.deleteAnalyses(idArray);
    }
    catch (NotFoundException nfe) {
      // validateOwnership throws not found if ID does not exist; convert to 400
      throw new BadRequestException(nfe.getMessage());
    }
  }

  private void performInheritGuestAnalyses(UserDataFactory dataFactory, User user, Long guestUserId) {
    if (guestUserId == null)
      return;
    if (user.isGuest())
      throw new BadRequestException("Guest users cannot inherit analyses.");
    dataFactory.addUserIfAbsent(user);
    dataFactory.transferGuestAnalysesOwnership(guestUserId, user.getUserID());
  }

  private static void editAnalysis(User user, AnalysisDetail analysis, SingleAnalysisPatchRequest entity) {
    boolean changeMade = false;
    if (entity.getIsPublic() != null) {
      if (user.isGuest() && entity.getIsPublic()) {
        throw new BadRequestException("Guest users cannot make their analyses public.");
      }
      changeMade = true; analysis.setIsPublic(entity.getIsPublic());
    }
    if (entity.getDisplayName() != null) {
      changeMade = true; analysis.setDisplayName(
          checkMaxSize(50, "displayName", checkNonEmpty("displayName", entity.getDisplayName())));
    }
    if (entity.getDescription() != null) {
      changeMade = true; analysis.setDescription(
          checkMaxSize(4000, "description", entity.getDescription()));
    }
    if (entity.getDescriptor() != null) {
      changeMade = true; analysis.setDescriptor(entity.getDescriptor());
    }
    if (entity.getNotes() != null) {
      changeMade = true; analysis.setNotes(entity.getNotes());
    }
    if (changeMade) {
      analysis.setModificationTime(Utils.getCurrentDateTimeString());
    }
  }

}
