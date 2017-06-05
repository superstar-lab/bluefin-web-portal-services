package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.UserPreference;

/**
 * @author mmishra
 *
 */
public interface UserPreferenceDAO {
	
	public Long findPreferenceIdByPreferenceKey(String preferenceKey);
	public UserPreference findUserPreferenceIdByPreferenceId(Long userId, long preferenceId);
	public UserPreference updateUserTimeZonePreference(UserPreference userPrefrence);
	public UserPreference insertUserTimeZonePreference(UserPreference userPrefrence);
	public String getSelectedTimeZone(Long userId);
}
