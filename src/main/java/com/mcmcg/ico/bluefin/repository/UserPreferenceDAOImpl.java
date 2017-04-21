package com.mcmcg.ico.bluefin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.UserPreference;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class UserPreferenceDAOImpl implements UserPreferenceDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserPreferenceDAOImpl.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Long findPreferenceIdByPreferenceKey(String prefKey) {
		Long preferenceKey = jdbcTemplate.queryForObject(Queries.findPreferenceIdByPreferenceKey, new Object[] { prefKey },  Long.class);
		return preferenceKey;
	}

	@Override
	public UserPreference findUserPreferenceIdByPreferenceId(Long userId, long preferenceId) {
		try {
		return jdbcTemplate.queryForObject(Queries.findPreferenceIdByPreferenceId,
				new Object[] { userId, preferenceId }, new UserPreferenceRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public UserPreference updateUserTimeZonePreference(UserPreference userPrefrence) {
		LOGGER.debug("Updating User Preference, UserPreferenceId - "+(userPrefrence.getUserPrefeenceID()));
		int rows = jdbcTemplate.update(Queries.updateUserPreference,
					new Object[] { 	userPrefrence.getPreferenceValue(), userPrefrence.getUserPrefeenceID() });
		LOGGER.debug("Updated UserPreference, No of Rows Updated " + rows);
		return userPrefrence;
	}

	@Override
	public UserPreference insertUserTimeZonePreference(UserPreference userPrefrence) {
		KeyHolder holder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {

			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(Queries.saveUserPreference,
						Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, userPrefrence.getPreferenceKeyID()); // PreferenceKeyID
				ps.setString(2, userPrefrence.getPreferenceValue()); //PreferenceValue
				ps.setLong(3, userPrefrence.getUserID()); //UserID
				return ps;
			}
		}, holder);
		
		Long id = holder.getKey().longValue();
		userPrefrence.setUserPrefeenceID(id);
		LOGGER.info("Saved UserPreference - id: " + id);
		return userPrefrence;
	}

	@Override
	public String getSelectedTimeZone(Long userId) {
		try {
			return jdbcTemplate.queryForObject(Queries.findSelectedTimeZoneByUserId, new Object[] { userId },  String.class);
		} catch (EmptyResultDataAccessException e) {
			LOGGER.debug("No time zone saved for userid="+userId);
			return null;
		}
	}
	
}

class UserPreferenceRowMapper implements RowMapper<UserPreference> {
	@Override
	public UserPreference mapRow(ResultSet rs, int row) throws SQLException {
		UserPreference userPreference = new UserPreference();
		userPreference.setUserPrefeenceID(rs.getLong("UserPreferenceID"));
		userPreference.setUserID(rs.getLong("UserID"));
		userPreference.setPreferenceKeyID(rs.getLong("PreferenceID"));
		userPreference.setPreferenceValue(rs.getString("PreferenceValue"));
		
		return userPreference;
	}
}