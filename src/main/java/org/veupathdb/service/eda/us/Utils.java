package org.veupathdb.service.eda.us;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Request;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.providers.UserProvider;

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
}
