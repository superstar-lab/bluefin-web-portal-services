package com.mcmcg.ico.bluefin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

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
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp loginDateTime = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(new PreparedStatementCreator() {

			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(Queries.saveUserLoginHistory,
						Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, userLoginHistory.getUserId()); // UserID
				ps.setTimestamp(2, loginDateTime); // LoginDateTime
				ps.setTimestamp(3, dateCreated); // DateCreated
				ps.setInt(4, userLoginHistory.getMessageId()); // MessageID
				ps.setString(5, userLoginHistory.getUsername()); // UserName
				ps.setString(6, userLoginHistory.getUserPassword()); // UserPassword
				return ps;
			}
		}, holder);

		Long id = holder.getKey().longValue();
		userLoginHistory.setUserLoginHistoryId(id);
		LOGGER.debug("Saved userLoginHistory - id: " + id);

		return id;
	}
}
