package org.veupathdb.service.eda.us.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.accountdb.AccountManager;
import org.gusdb.fgputil.accountdb.UserProfile;
import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.veupathdb.lib.container.jaxrs.utils.db.DbManager;
import org.veupathdb.service.eda.generated.model.AnalysisSummaryWithUser;

public class AccountDbData {

  private static final List<UserPropertyName> USER_PROPERTIES = Arrays.asList(new UserPropertyName[] {
    new UserPropertyName("firstName", "first_name", true),
    new UserPropertyName("middleName", "middle_name", false),
    new UserPropertyName("lastName", "last_name", true),
    new UserPropertyName("organization", "organization", true),
  });

  private static class AccountDataPair extends TwoTuple<String,String> {
    public AccountDataPair(Map<String,String> props) {
      super(
        getDisplayName(props),
        Optional.ofNullable(props.get("organization")).orElse("")
      );
    }
    public String getName() { return getFirst(); }
    public String getOrganization() { return getSecond(); }
  }

  private final Map<Long, Optional<AccountDataPair>> _accountDataCache = new HashMap<>();

  public List<AnalysisSummaryWithUser> populateOwnerData(List<AnalysisSummaryWithUserAndId> analyses) {
    AccountManager acctDb = new AccountManager(DbManager.accountDatabase(), "useraccounts.", USER_PROPERTIES);
    return analyses.stream()
      .peek(analysis -> {
        Optional<AccountDataPair> accountData = _accountDataCache.get(analysis.getUserId());
        if (accountData == null) {
          UserProfile profile = acctDb.getUserProfile(analysis.getUserId());
          accountData = profile == null || profile.isGuest() ? Optional.empty() :
              Optional.of(new AccountDataPair(profile.getProperties()));
          _accountDataCache.put(analysis.getUserId(), accountData);
        }
        // skip value population for guests and deleted(?) users
        accountData.ifPresent(data -> {
          analysis.setUserName(data.getName());
          analysis.setUserOrganization(data.getOrganization());
        });
      })
      .collect(Collectors.toList());
  }

  /**
   * Copy of the method in the EuPathDB OAuth impl that performs this task
   *
   * @param userProperties set of user properties
   * @return displayable name value that combines first/middle/last name values
   */
  private static String getDisplayName(Map<String,String> userProperties) {
    String firstName = userProperties.get("firstName");
    String middleName = userProperties.get("middleName");
    String lastName = userProperties.get("lastName");
    String name = null;
    if (firstName != null && !firstName.isEmpty()) name = firstName;
    if (middleName != null && !middleName.isEmpty()) {
      name = (name == null ? middleName : name + " " + middleName);
    }
    if (lastName != null && !lastName.isEmpty()) {
      name = (name == null ? lastName : name + " " + lastName);
    }
    return name;
  }
}
