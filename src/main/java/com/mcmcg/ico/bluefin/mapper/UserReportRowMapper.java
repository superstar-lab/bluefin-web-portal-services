package com.mcmcg.ico.bluefin.mapper;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.User;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserReportRowMapper implements RowMapper<User> {

	DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);

	@Override
	public User mapRow(ResultSet rs, int row) throws SQLException {
		User user = new User();
		user.setUsername(rs.getString("UserName"));
		user.setFirstName(rs.getString("FirstName"));
		user.setLastName(rs.getString("LastName"));
		user.setRoleName(rs.getString("RoleName"));
		user.setEmail(rs.getString("Email"));

		Timestamp ts;
		if (rs.getString(BluefinWebPortalConstants.DATECREATED) != null) {
			ts = Timestamp.valueOf(rs.getString(BluefinWebPortalConstants.DATECREATED));
			user.setDateCreated(new DateTime(ts));
		}

		if (rs.getString("DateModified") != null) {
			ts = Timestamp.valueOf(rs.getString("DateModified"));
			user.setDateModified(new DateTime(ts));
		}

		if (rs.getString("LastLogin") != null) {
			ts = Timestamp.valueOf(rs.getString("LastLogin"));
			user.setLastLogin(new DateTime(ts));
		}

		user.setStatus(rs.getString("Status"));

		//Chance: Add the password last date modified to USER POJO.
		if (rs.getString("PH_DatedModified") != null) {
			ts = Timestamp.valueOf(rs.getString("PH_DatedModified"));
			user.setLastDatePasswordModified(new DateTime(ts));
		}

		return user;
	}
	
	
}