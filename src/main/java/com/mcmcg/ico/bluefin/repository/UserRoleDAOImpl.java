package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class UserRoleDAOImpl implements UserRoleDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleDAOImpl.class);
	private final DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private NamedParameterJdbcTemplate namedJDBCTemplate;
	@Override
	public void saveRoles(
			Collection<com.mcmcg.ico.bluefin.model.UserRole> userRoles) {
		insertBatch(new ArrayList<com.mcmcg.ico.bluefin.model.UserRole>(userRoles));
	}
	
	private void insertBatch(final List<com.mcmcg.ico.bluefin.model.UserRole> userRoles){
		jdbcTemplate.batchUpdate(Queries.SAVEUSERROLE, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				com.mcmcg.ico.bluefin.model.UserRole userRole = userRoles.get(i);
				DateTime utc1 = userRole.getDateModified() != null ? userRole.getDateModified().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
				Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
				LOGGER.debug("UserRoleDAOImpl :: insertBatch() : Creating child item for , UserId = "+(userRole.getUserId()));
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

	@Override
	public void deleteUserRoleById(Set<Long> rolesToRemove) {
		if(!rolesToRemove.isEmpty()) {
			Map<String, Set<Long>> valuesToDelete = new HashMap<>();
			valuesToDelete.put("userRoleIds", rolesToRemove);
			executeQueryToDeleteUserRoles(Queries.DELETEUSERROLES,valuesToDelete);
		}
	}
	
	private void executeQueryToDeleteUserRoles(String deleteQuery,Map<String, Set<Long>> idsToDelete){
		LOGGER.debug("UserRoleDAOImpl :: executeQueryToDeleteUserRoles() : Finally deleteing records, idsToDelete="+idsToDelete);
		
		int noOfRowsDeleted = namedJDBCTemplate.update(deleteQuery,idsToDelete);
		LOGGER.debug("UserRoleDAOImpl :: executeQueryToDeleteUserRoles() : Number of rows of roles deleted (Using user role id) ="+(noOfRowsDeleted));
	}
	
	@Override
	public List<UserRole> findByUserId(long userId) {
		ArrayList<UserRole> list = (ArrayList<UserRole>) jdbcTemplate.query(Queries.FINDUSERROLEBYUSERID,
				new Object[] { userId }, new RowMapperResultSetExtractor<UserRole>(new UserRoleRowMapper()));

		LOGGER.debug("UserRoleDAOImpl :: findByUserId() : Number of rows: " + list.size());

		return list;
	}
}

class UserRoleRowMapper implements RowMapper<UserRole> {

	@Override
	public UserRole mapRow(ResultSet rs, int row) throws SQLException {
		UserRole userRole = new UserRole();
		userRole.setUserRoleId(rs.getLong("UserRoleID"));
		userRole.setUserId(rs.getLong("UserID"));
		userRole.setRoleId(rs.getLong("RoleID"));
		Timestamp ts;

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
