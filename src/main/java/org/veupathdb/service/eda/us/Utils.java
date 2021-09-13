package org.veupathdb.service.eda.us;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Request;
import org.gusdb.fgputil.FormatUtil;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.providers.UserProvider;
import org.veupathdb.service.eda.us.model.AnalysisDetailWithUser;
import org.veupathdb.service.eda.us.model.UserDataFactory;

public class Utils {

  public static User getActiveUser(Request request) {
    return UserProvider.lookupUser(request).orElseThrow(() ->
        // authentication framework should handle cases where no appropriate user header was sent
        new InternalServerErrorException("Request reached authenticated endpoint with no user attached"));
  }

  public static User getAuthorizedUser(Request request, String resourceUserId) {
    User activeUser = getActiveUser(request);
    if (!String.valueOf(activeUser.getUserID()).equals(resourceUserId)) {
      throw new ForbiddenException();
    }
    return activeUser;
  }

  public static <T, E extends RuntimeException> T doThrow(E exception) {
    throw exception;
  }

  public static String getCurrentDateTime() {
    return FormatUtil.formatDateTime(new Date());
  }

  public static String getDateString(Timestamp timestamp) {
    return FormatUtil.formatDateTime(new Date(timestamp.getTime()));
  }

  public static void verifyOwnership(long userId, String... ids) {
    List<String> usersAnalysisIds = UserDataFactory.getAnalysisSummaries(userId).stream()
        .map(sum -> sum.getAnalysisId()).collect(Collectors.toList());
    List<String> badIds = Arrays.stream(ids)
        .filter(id -> !usersAnalysisIds.contains(id)).collect(Collectors.toList());
    if (!badIds.isEmpty()) {
      throw new NotFoundException("Analysis ID(s) [ " + String.join(", ", badIds) + " ] cannot be found for user " + userId);
    }
  }

  public static void verifyOwnership(long userId, AnalysisDetailWithUser analysis) {
    if (userId != analysis.getUserId()) {
      throw new NotFoundException();
    }
  }
}
