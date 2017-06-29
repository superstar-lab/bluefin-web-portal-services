package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.UserLoginHistory;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class UserLoginHistoryDAOImpl implements UserLoginHistoryDAO {

	private static Logger LOGGER = LoggerFactory.getLogger(UserLoginHistoryDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public long saveUserLoginHistory(UserLoginHistory userLoginHistory) {
		KeyHolder holder = new GeneratedKeyHolder();

		// The Java class uses Joda DateTime, which isn't supported by
		// PreparedStatement.
		// Convert Joda DateTime to UTC (the format for the database).
		// Remove the 'T' and 'Z' from the format, because it's not in the
		// database.
		// Convert this string to Timestamp, which is supported by
		// PreparedStatement.
		DateTime utc1 = userLoginHistory.getLoginDateTime().withZone(DateTimeZone.UTC);
		DateTime utc2 = userLoginHistory.getDateCreated().withZone(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp loginDateTime = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc2));
		
		jdbcTemplate.update(connection->{
				PreparedStatement ps = connection.prepareStatement(Queries.saveUserLoginHistory,
						Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, userLoginHistory.getUserId()!=null?userLoginHistory.getUserId():Long.valueOf("0"));
				ps.setTimestamp(2, loginDateTime);
				ps.setTimestamp(3, dateCreated);
				ps.setInt(4, userLoginHistory.getMessageId());
				ps.setString(5, userLoginHistory.getUsername());
				ps.setString(6, userLoginHistory.getPassword());
				return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		userLoginHistory.setUserLoginHistoryId(id);
		LOGGER.debug("UserLoginHistoryDAOImpl :: saveUserLoginHistory() : Saved userLoginHistory - id: " + id);

		return id;
	}
}
