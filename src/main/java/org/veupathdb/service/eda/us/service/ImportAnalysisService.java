package org.veupathdb.service.eda.us.service;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.FormatUtil;
import org.json.JSONObject;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.providers.UserProvider;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.service.eda.common.client.DatasetAccessClient;
import org.veupathdb.service.eda.generated.model.AnalysisListPostResponse;
import org.veupathdb.service.eda.generated.model.SingleAnalysisPublicInfo;
import org.veupathdb.service.eda.generated.model.SingleAnalysisPublicInfoImpl;
import org.veupathdb.service.eda.generated.resources.ImportAnalysisProjectId;
import org.veupathdb.service.eda.us.Resources;
import org.veupathdb.service.eda.us.Utils;
import org.veupathdb.service.eda.us.model.*;

import java.util.Optional;

import static org.gusdb.fgputil.functional.Functions.doThrow;

@Authenticated(allowGuests = true)
public class ImportAnalysisService implements ImportAnalysisProjectId {

  @Context
  private ContainerRequest _request;

  @Override
  public GetImportAnalysisByProjectIdAndAnalysisIdResponse getImportAnalysisByProjectIdAndAnalysisId(String projectId, String analysisId) {
    return GetImportAnalysisByProjectIdAndAnalysisIdResponse.respond200WithApplicationJson(
        importAnalysis(projectId, analysisId, Optional.empty(), _request));
  }

  @Override
  public GetImportAnalysisInfoByProjectIdAndAnalysisIdResponse getImportAnalysisInfoByProjectIdAndAnalysisId(String projectId, String analysisId) {
    AnalysisDetailWithUser analysis = new UserDataFactory(projectId).getAnalysisById(analysisId);
    SingleAnalysisPublicInfo info = new SingleAnalysisPublicInfoImpl();
    info.setStudyId(analysis.getStudyId());
    return GetImportAnalysisInfoByProjectIdAndAnalysisIdResponse.respond200WithApplicationJson(info);
  }

  /**
   * After validating params, copies the analysis with the passed ID to a new analysis owned by the
   * active user (i.e. whose credentials were provided.  The optional userID is for an endpoing whose
   * path includes both userId and analysisId; so if present the analysis being copied must be owned
   * by the user passed in (or 404 will be thrown).  The copier's permission to access the study of
   * the analysis at the "visualization" level is also checked, with a 403 result if disallowed.
   *
   * @param projectId project ID under which analysis is stored
   * @param analysisId ID of analysis to be copied
   * @param userIdOpt ID of owner of analysis to be copied (not required, but verified if provided)
   * @param request container request, used to look up submitted credentials
   * @return response describing newly created analysis
   */
  public static AnalysisListPostResponse importAnalysis(String projectId, String analysisId, Optional<String> userIdOpt, ContainerRequest request) {

    // create data factory (validates projectId)
    UserDataFactory dataFactory = new UserDataFactory(projectId);

    // look up analysis by ID
    AnalysisDetailWithUser oldAnalysis = dataFactory.getAnalysisById(analysisId);

    // if provided, verify URL's userId and analysisId match
    long userId = userIdOpt.map(userIdStr -> {
        long verifiedId = FormatUtil.isLong(userIdStr) ? Long.valueOf(userIdStr) : doThrow(NotFoundException::new);
        Utils.verifyOwnership(verifiedId, oldAnalysis);
        return verifiedId;
    }).orElse(oldAnalysis.getUserId());

    // make sure user importing has access to this analysis' study
    requireStudyAccess(request, oldAnalysis.getStudyId());

    // make a copy of the analysis, assign a new owner, check display name (must be unique) and insert
    User newOwner = Utils.getActiveUser(request);
    dataFactory.addUserIfAbsent(newOwner);
    AccountDbData.AccountDataPair provenanceOwner = new AccountDbData().getUserDataById(userId);
    AnalysisDetailWithUser newAnalysis = new AnalysisDetailWithUser(
        IdGenerator.getNextAnalysisId(dataFactory), newOwner.getUserID(), oldAnalysis, provenanceOwner);

    // If the new owner is not the same as the old owner, we need to create
    // copies of all the derived variables.
    if (newOwner.getUserID() != userId) {
      var descriptor = newAnalysis.getDescriptor();
      var variables  = dataFactory.getDerivedVariables(descriptor.getDerivedVariables());

      // If the descriptor has derived variables.
      if (!variables.isEmpty()) {
        // For each variable, issue a new ID and assign the new user ID, keeping
        // the other fields unchanged.
        variables.forEach(v -> {
          v.setVariableID(Utils.issueUUID());
          v.setUserID(newOwner.getUserID());
        });

        // Insert the modified derived variables
        dataFactory.addDerivedVariables(variables);

        // Replace the descriptor's derived variable IDs with the newly issued
        // IDs.
        descriptor.setDerivedVariables(variables.stream().map(DerivedVariableRow::getVariableID).toList());
      }
    }

    dataFactory.insertAnalysis(newAnalysis);
    return newAnalysis.getIdObject();
  }

  static void requireStudyAccess(ContainerRequest request, String studyID) {
    try {
      DatasetAccessClient.BasicStudyDatasetInfo info = new DatasetAccessClient(
        Resources.DATASET_ACCESS_SERVICE_URL,
        UserProvider.getSubmittedAuth(request).orElseThrow() // should already have been authenticated
      )
        // NOTE: even though we're calling getStudyId(), this method currently returns a dataset ID!!
        .getStudyPermsByDatasetId(studyID);

      if (!info.getStudyAccess().allowSubsetting()) {
        throw new ForbiddenException(new JSONObject()
          .put("denialReason", "noAccess")
          .put("message", "The requesting user does not have access to this study.")
          .put("datasetId", studyID)
          .put("isUserDataset", info.isUserStudy())
          .toString()
        );
      }
    }
    catch (NotFoundException e) {
      // per https://github.com/VEuPathDB/EdaUserService/issues/24 if dataset under the study does not exist, throw Forbidden
      throw new ForbiddenException(new JSONObject()
        .put("denialReason", "missingDataset")
        .put("message", "This analysis cannot be imported because the underlying dataset '" + studyID + "' no longer exists.")
        .put("datasetId", studyID)
        .toString());
    }

  }
}
