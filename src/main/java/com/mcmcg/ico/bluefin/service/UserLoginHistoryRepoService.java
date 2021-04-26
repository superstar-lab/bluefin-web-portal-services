package com.mcmcg.ico.bluefin.service;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.enums.UserStatus;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLoginHistory;
import com.mcmcg.ico.bluefin.repository.UserLoginHistoryDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class UserLoginHistoryRepoService {

    @Autowired
    private UserLoginHistoryDAO userLoginHistoryDAO;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginHistoryRepoService.class);

    public void saveUserLoginHistory(UserLoginHistory userLoginHistory, Integer messageCode) {
        LOGGER.debug("userLoginHistory value is ={} ",userLoginHistory);
        if (userLoginHistory != null) {
            userLoginHistory.setMessageId(messageCode);
            userLoginHistoryDAO.saveUserLoginHistory(userLoginHistory);
        }
        LOGGER.info("Exit from saveUserLoginHistory");
    }

    public void saveUserLoginHistoryAuthentication(User user, UserLoginHistory userLoginHistory, String userName){
        String message="";
        if (user == null) {
            message = LoggingUtil.invalidLoginAttempts("User: ", userName, BluefinWebPortalConstants.SEPARATOR,
                    "Reason : User NOT FOUND");
            LOGGER.error(message);

            saveUserLoginHistory(userLoginHistory, UserLoginHistory.MessageCode.ERROR_USER_NOT_FOUND.getValue());
            throw new CustomUnauthorizedException("Invalid credentials");
        }
        userLoginHistory.setUserId(user.getUserId());
        if ("NEW".equals(user.getStatus())) {
            message = LoggingUtil.invalidLoginAttempts("UserName: ", user.getUsername(), BluefinWebPortalConstants.SEPARATOR,
                    "Reason : User is: ", UserStatus.NEW.getStatus());
            LOGGER.error(message);

            saveUserLoginHistory(userLoginHistory, UserLoginHistory.MessageCode.ERROR_USER_NOT_ACTIVE.getValue());
            throw new AccessDeniedException("Account is not activated yet.");
        }
        if ("INACTIVE".equals(user.getStatus())) {
            message = LoggingUtil.invalidLoginAttempts("User:: ", user.getUsername(), BluefinWebPortalConstants.SEPARATOR,
                    "Reason : User is:: ", UserStatus.INACTIVE.getStatus());
            LOGGER.error(message);

            saveUserLoginHistory(userLoginHistory, UserLoginHistory.MessageCode.ERROR_USER_NOT_ACTIVE.getValue());
            throw new AccessDeniedException("Account was deactivated.");
        }
    }

    public void saveUserLoginHistoryFailAuthentication(User user, UserLoginHistory userLoginHistory, String userName) {
        saveUserLoginHistoryAuthentication(user, userLoginHistory, userName);
        if(user != null) {
            if ("LOCKED".equals(user.getStatus())) {
                String message = LoggingUtil.invalidLoginAttempts("User:: ", user.getUsername(), BluefinWebPortalConstants.SEPARATOR,
                        "Reason : User is:: ", UserStatus.INACTIVE.getStatus());
                LOGGER.error(message);
                saveUserLoginHistory(userLoginHistory, UserLoginHistory.MessageCode.ERROR_USER_NOT_ACTIVE.getValue());
                throw new AccessDeniedException("Account was deactivated.");
            }
        }
    }
}
