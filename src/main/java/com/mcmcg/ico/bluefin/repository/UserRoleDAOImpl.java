package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class UserRoleDAOImpl implements UserRoleDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleDAOImpl.class);
	private final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<UserRole> findByUserId(long userId) {
		ArrayList<UserRole> list = (ArrayList<UserRole>) jdbcTemplate.query(Queries.findUserRoleByUserId,
				new Object[] { userId }, new RowMapperResultSetExtractor<UserRole>(new UserRoleRowMapper()));

		LOGGER.debug("Number of rows: " + list.size());

		return list;
	}

	@Override
	public List<UserRole> findByRoleId(long roleId) {
		ArrayList<UserRole> list = (ArrayList<UserRole>) jdbcTemplate.query(Queries.findUserRoleByRoleId,
				new Object[] { roleId }, new RowMapperResultSetExtractor<UserRole>(new UserRoleRowMapper()));

		LOGGER.debug("Number of rows: " + list.size());

		return list;
	}

	@Override
	public void saveRoles(
			Collection<com.mcmcg.ico.bluefin.model.UserRole> userRoles) {
		insertBatch(new ArrayList<com.mcmcg.ico.bluefin.model.UserRole>(userRoles));
	}
	
	private void insertBatch(final List<com.mcmcg.ico.bluefin.model.UserRole> userRoles){
		jdbcTemplate.batchUpdate(Queries.saveUserRole, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				com.mcmcg.ico.bluefin.model.UserRole userRole = userRoles.get(i);
				DateTime utc1 = userRole.getDateModified() != null ? userRole.getDateModified().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
				Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
				LOGGER.info("Creating child item for , UserId = "+(userRole.getUserId()));
				ps.setLong(1, userRole.getUser().getUserId());
				ps.setLong(2, userRole.getRole().getRoleId());
				ps.setTimestamp(3, dateCreated);
			}

			@Override
			public int getBatchSize() {
				return userRoles.size();
			}
		  });
	}
}

class UserRoleRowMapper implements RowMapper<UserRole> {

	@Override
	public UserRole mapRow(ResultSet rs, int row) throws SQLException {
		UserRole userRole = new UserRole();
		userRole.setRoleId(rs.getLong("UserRoleID"));
		userRole.setRoleId(rs.getLong("UserID"));
		userRole.setRoleId(rs.getLong("RoleID"));
		Timestamp ts = null;

		if (rs.getString("DateCreated") != null) {

			ts = Timestamp.valueOf(rs.getString("DateCreated"));
			userRole.setDateCreated(new DateTime(ts));
		}
		if (rs.getString("DatedModified") != null) {

			ts = Timestamp.valueOf(rs.getString("DatedModified"));
			userRole.setDateModified(new DateTime(ts));
		}
		userRole.setModifiedBy(rs.getString("ModifiedBy"));

		return userRole;
	}
}
