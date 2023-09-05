package org.veupathdb.service.eda.us.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.StringUtil;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.service.eda.generated.model.*;
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

    // Store off a reference to the original derived variable ID list to use to
    // compare to the potential new list later.
    var originalDerivedVars = getDerivedVariables(analysis);

    editAnalysis(user, analysis, entity);

    var newDerivedVars = processPatchedDerivedVars(user, dataFactory, analysis, originalDerivedVars, getDerivedVariables(analysis));
    if (!newDerivedVars.isEmpty()) {
      if (analysis.getDescriptor() == null)
        analysis.setDescriptor(new AnalysisDescriptorImpl());

      analysis.getDescriptor().setDerivedVariables(newDerivedVars);
    }

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
      changeMade = true;
      analysis.setDescriptor(entity.getDescriptor());

      // Validate any patched in derived variable IDs
      for (var derivedVarID : getDerivedVariables(analysis))
        if (!StringUtil.isUuid(derivedVarID))
          throw new BadRequestException("derived variable id " + derivedVarID + "is invalid");
    }
    if (entity.getNotes() != null) {
      changeMade = true; analysis.setNotes(entity.getNotes());
    }
    if (changeMade) {
      analysis.setModificationTime(Utils.getCurrentDateTimeString());
    }
  }

  private static List<String> getDerivedVariables(AnalysisDetail analysis) {
    return Optional.of(analysis.getDescriptor())
      .map(AnalysisDescriptor::getDerivedVariables)
      .orElseGet(Collections::emptyList);
  }

  /**
   * Compares the given lists of derived variable IDs and, if they differ,
   * unions them into a new list containing all the distinct ID values.
   *
   * @param user Target user record.
   * @param dataFactory User data factory used to perform database lookups to
   *   test the validity of any new derived variable IDs.
   * @param analysis Target analysis to which the derived variables are being
   *   attached.
   * @param oldIDs The original list of derived variable IDs attached to a given
   *   entity.
   * @param newIDs The new list of derived variable IDs that was sent in to the
   *   API by the client.
   *
   * @return The unioned list of the two input lists of derived variable IDs.
   */
  private static List<String> processPatchedDerivedVars(
    User user,
    UserDataFactory dataFactory,
    AnalysisDetailWithUser analysis,
    List<String> oldIDs,
    List<String> newIDs
  ) {
    // If the list of derived variables hasn't changed any, then there's nothing
    // for us to do.  Yay!
    if (oldIDs.equals(newIDs))
      return oldIDs;

    // If the new list of derived variables differs from the original list,
    // validate the derived variables in the list then union the new list with
    // the original list to form the new list of derived vars.

    var newIDSet = new HashSet<>(newIDs);

    // Remove any overlap with the original set of derived variables so that
    // we aren't doing any needless validation work (we will add them back
    // later).
    //
    // NOTE: Using a forEach here instead of Collection::removeAll as the
    // HashSet implementation of removeAll is far less performant.
    oldIDs.forEach(newIDSet::remove);

    // Fetch a list of all the matching derived variables from the database.
    // NOTE: At this point the derived variable IDs are all known to be valid
    // UUID values, which means SQL injection here is not possible.
    var dbDerivedVars = dataFactory.getDerivedVariables(newIDSet);

    // If we didn't find all the derived variables that we searched for, then
    // one or more of them are invalid.
    if (dbDerivedVars.size() != newIDSet.size())
      throw new BadRequestException("one or more of the derived variable IDs provided do not exist");

    // Verify that the derived variables that the client requested all belong to
    // the target user and study.
    for (var derivedVariableRow : dbDerivedVars) {
      if (derivedVariableRow.getUserID() != user.getUserID())
        throw new BadRequestException("one or more of the given derived variable IDs does not belong to the target user");

      if (!derivedVariableRow.getDatasetID().equals(analysis.getStudyId()))
        throw new BadRequestException("one or more of the given derived variable IDs does not belong to the target study");
    }

    // At this point we know that all the given derived variable IDs exist in
    // the database and are attached to the target user and dataset.  Go ahead
    // and add back the original IDs so we can write the full list of attached
    // derived variables back to the database.
    newIDSet.addAll(oldIDs);

    return newIDSet.stream().toList();
  }

}
