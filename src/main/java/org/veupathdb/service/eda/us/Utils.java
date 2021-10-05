package org.veupathdb.service.eda.us;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Request;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.json.JsonUtil;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.providers.UserProvider;
import org.veupathdb.service.eda.generated.model.AnalysisDescriptor;
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

  public static String getCurrentDateTimeString() {
    return FormatUtil.formatDateTime(new Date());
  }

  public static String formatTimestamp(Timestamp timestamp) {
    return FormatUtil.formatDateTime(new Date(timestamp.getTime()));
  }

  public static Date parseDate(String dateString) {
    try {
      return FormatUtil.STANDARD_DATE_TIME_TEXT_FORMAT.get().parse(dateString);
    }
    catch (ParseException e) {
      throw new BadRequestException();
    }
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

  public static AnalysisDescriptor parseDescriptor(String descriptorStr) {
    try {
      return JsonUtil.Jackson.readValue(descriptorStr, AnalysisDescriptor.class);
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException("Could not map descriptor string to an AnalysisDescriptor object", e);
    }
  }

  public static String formatDescriptor(AnalysisDescriptor descriptor) {
    try {
      return JsonUtil.Jackson.writerFor(AnalysisDescriptor.class).writeValueAsString(descriptor);
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException("Could not serialize analysis descriptor", e);
    }
  }

  public static String checkNonEmpty(String key, String value) {
    if (value == null || value.trim().isEmpty()) {
      throw new BadRequestException(key + " cannot be empty.");
    }
    return value.trim();
  }
}
