package com.mcmcg.ico.bluefin.security.service;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.enums.UserStatus;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLoginHistory;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.service.PropertyService;
import com.mcmcg.ico.bluefin.service.UserLoginHistoryRepoService;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordUtilsService {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private UserLookUpService userLookUpService;

    @Autowired
    UserLoginHistoryRepoService userLoginHistoryRepoService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordUtilsService.class);

    public void updateUserLookUp(User user, UserLoginHistory userLoginHistory) {
        if (user != null) {
            String wrongPasswordMaxLimit;
            String message = "";
            int wrongPasswordMaxLimitVal;

            Integer wrongPasswordCounterNextVal = user.getWrongPasswordCounter() + 1;
            wrongPasswordMaxLimit = propertyService.getPropertyValue(BluefinWebPortalConstants.WRONGPWMAXLIMIT);
            try {
                wrongPasswordMaxLimitVal = wrongPasswordMaxLimit == null || "".equals(wrongPasswordMaxLimit.trim()) ?
                        BluefinWebPortalConstants.WRONGPASSWORDMAXLIMITDEFAULT : Integer.parseInt(wrongPasswordMaxLimit);
                if (wrongPasswordMaxLimitVal < 0)
                    wrongPasswordMaxLimitVal = BluefinWebPortalConstants.WRONGPASSWORDMAXLIMITDEFAULT;
            } catch (NumberFormatException e) {
                wrongPasswordMaxLimitVal = BluefinWebPortalConstants.WRONGPASSWORDMAXLIMITDEFAULT;
            }

            if (wrongPasswordCounterNextVal >= wrongPasswordMaxLimitVal) {
                user.setStatus(UserStatus.LOCKED.getStatus());
                user.setAccountLockedOn(new DateTime(DateTimeZone.UTC));
                message = LoggingUtil.invalidLoginAttempts("User: ", user.getUsername(), BluefinWebPortalConstants.SEPARATOR,
                        "Reason : ", "PASSWORD IS INVALID", BluefinWebPortalConstants.SEPARATOR,
                        "WRONG PASSWORD ATTEMPTS : ", String.valueOf(wrongPasswordCounterNextVal), BluefinWebPortalConstants.SEPARATOR,
                        "User is LOCKED");
                LOGGER.error(message);
            } else {
                message = LoggingUtil.invalidLoginAttempts("User:: ", user.getUsername(), BluefinWebPortalConstants.SEPARATOR,
                        "Reason : ", "PASSWORD IS INVALID", BluefinWebPortalConstants.SEPARATOR,
                        "WRONG PASSWORD ATTEMPTS : ", String.valueOf(wrongPasswordCounterNextVal));
                LOGGER.error(message);
            }
            user.setWrongPasswordCounter(wrongPasswordCounterNextVal);
            userLoginHistoryRepoService.saveUserLoginHistory(userLoginHistory, UserLoginHistory.MessageCode.ERROR_PASSWORD_NOT_FOUND.getValue());
            userLookUpService.updateUserLookUp(user);
            throw new CustomUnauthorizedException("Invalid credentials");
        }
    }
}
