package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.UserLoginHistory;

public interface UserLoginHistoryDAO {
	long saveUserLoginHistory(UserLoginHistory userLoginHistory);
}
