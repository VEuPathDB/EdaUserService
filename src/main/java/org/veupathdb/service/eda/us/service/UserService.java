package org.veupathdb.service.eda.us.service;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.service.eda.generated.model.AnalysisListPatchRequest;
import org.veupathdb.service.eda.generated.model.AnalysisListPostRequest;
import org.veupathdb.service.eda.generated.model.SingleAnalyisPatchRequest;
import org.veupathdb.service.eda.generated.resources.UsersUserId;
import org.veupathdb.service.eda.us.Utils;
import org.veupathdb.service.eda.us.model.UserDataFactory;

@Authenticated(allowGuests = true)
public class UserService implements UsersUserId {

  @Context
  private Request _request;

  @Override
  public GetUsersPreferencesByUserIdResponse getUsersPreferencesByUserId(String userId) {
    String prefs = UserDataFactory.readPreferences(Utils.getAuthorizedUser(_request, userId));
    return GetUsersPreferencesByUserIdResponse.respond200WithApplicationJson(prefs);
  }

  @Override
  public PutUsersPreferencesByUserIdResponse putUsersPreferencesByUserId(String userId, String entity) {
    UserDataFactory.writePreferences(Utils.getAuthorizedUser(_request, userId), entity);
    return PutUsersPreferencesByUserIdResponse.respond202();
  }

  @Override
  public GetUsersAnalysesByUserIdResponse getUsersAnalysesByUserId(String userId) {
    return null;
  }

  @Override
  public PostUsersAnalysesByUserIdResponse postUsersAnalysesByUserId(String userId, AnalysisListPostRequest entity) {
    return null;
  }

  @Override
  public PatchUsersAnalysesByUserIdResponse patchUsersAnalysesByUserId(String userId, AnalysisListPatchRequest entity) {
    return null;
  }

  @Override
  public GetUsersAnalysesByUserIdAndAnalysisIdResponse getUsersAnalysesByUserIdAndAnalysisId(String userId, String analysisId) {
    return null;
  }

  @Override
  public PatchUsersAnalysesByUserIdAndAnalysisIdResponse patchUsersAnalysesByUserIdAndAnalysisId(String userId, String analysisId, SingleAnalyisPatchRequest entity) {
    return null;
  }

  @Override
  public DeleteUsersAnalysesByUserIdAndAnalysisIdResponse deleteUsersAnalysesByUserIdAndAnalysisId(String userId, String analysisId) {
    return null;
  }

  @Override
  public PostUsersAnalysesCopyByUserIdAndAnalysisIdResponse postUsersAnalysesCopyByUserIdAndAnalysisId(String userId, String analysisId) {
    return null;
  }
}
