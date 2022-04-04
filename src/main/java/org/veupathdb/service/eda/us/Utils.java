package org.veupathdb.service.eda.us;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.json.JsonUtil;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.providers.UserProvider;
import org.veupathdb.service.eda.generated.model.AnalysisSummary;
import org.veupathdb.service.eda.us.model.AnalysisDetailWithUser;
import org.veupathdb.service.eda.us.model.UserDataFactory;

public class Utils {

  public static User getActiveUser(ContainerRequest request) {
    return UserProvider.lookupUser(request).orElseThrow(() ->
        // authentication framework should handle cases where no appropriate user header was sent
        new InternalServerErrorException("Request reached authenticated endpoint with no user attached"));
  }

  public static User getAuthorizedUser(ContainerRequest request, String resourceUserId) {
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
    return FormatUtil.formatDateTimeNoTimezone(new Date());
  }

  public static String formatTimestamp(Timestamp timestamp) {
    return FormatUtil.formatDateTimeNoTimezone(new Date(timestamp.getTime()));
  }

  public static Date parseDate(String dateString) {
    try {
      return FormatUtil.STANDARD_DATE_TIME_TEXT_FORMAT.get().parse(dateString);
    }
    catch (ParseException e) {
      throw new BadRequestException();
    }
  }

  public static void verifyOwnership(UserDataFactory dataFactory, long userId, String... ids) {
    List<String> usersAnalysisIds = dataFactory.getAnalysisSummaries(userId).stream()
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

  public static <T> T parseObject(String jsonString, Class<T> clazz) {
    try {
      return jsonString == null ? null : JsonUtil.Jackson.readValue(jsonString, clazz);
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException("Could not map JSON string to a " + clazz.getName() + " object", e);
    }
  }

  public static String formatObject(Object obj) {
    try {
      return JsonUtil.Jackson.writeValueAsString(obj);
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException("Could not serialize " + obj.getClass().getName(), e);
    }
  }

  public static String checkNonEmpty(String key, String value) {
    if (value == null || value.trim().isEmpty()) {
      throw new BadRequestException(key + " must not be empty.");
    }
    return value.trim();
  }

  public static String checkMaxSize(int maxSize, String key, String value) {
    if (value != null && FormatUtil.getUtf8EncodedBytes(value).length > maxSize) {
      throw new BadRequestException(key + " must not be larger than " + maxSize + " bytes.");
    }
    return value;
  }
}
