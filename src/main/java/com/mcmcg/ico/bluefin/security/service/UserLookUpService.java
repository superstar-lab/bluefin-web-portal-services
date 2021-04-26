package com.mcmcg.ico.bluefin.security.service;

import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserLookUpService {

    @Autowired
    private UserDAO userDAO;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserLookUpService.class);

    public void updateUserLookUp(User user) {
        LOGGER.info("UserLookUpService -> updateUserLookUp, Inside updateUserLookUp method for User Id : " + user.getUserId());
        try {
            userDAO.updateUserLookUp(user);
        } catch (Exception e) {
            LOGGER.error("UserLookUpService -> updateUserLookUp error updating user: " +  user.getUserId(), e.getMessage(), e);
        }
    }
}
