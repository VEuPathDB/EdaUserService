package org.veupathdb.service.eda.us.service;

import org.veupathdb.service.eda.generated.model.AnalysisListPatchRequest;
import org.veupathdb.service.eda.generated.model.AnalysisListPostRequest;
import org.veupathdb.service.eda.generated.model.SingleAnalyisPatchRequest;
import org.veupathdb.service.eda.generated.model.UserPreferencesObject;
import org.veupathdb.service.eda.generated.resources.UsersUserId;

public class UserService implements UsersUserId {

  @Override
  public GetUsersPreferencesByUserIdResponse getUsersPreferencesByUserId(String userId) {
    return null;
  }

  @Override
  public PutUsersPreferencesByUserIdResponse putUsersPreferencesByUserId(String userId, UserPreferencesObject entity) {
    return null;
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
}
