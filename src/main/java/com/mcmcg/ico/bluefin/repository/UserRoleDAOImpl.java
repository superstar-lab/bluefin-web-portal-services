package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class UserRoleDAOImpl implements UserRoleDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleDAOImpl.class);

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
}

class UserRoleRowMapper implements RowMapper<UserRole> {

	@Override
	public UserRole mapRow(ResultSet rs, int row) throws SQLException {
		UserRole userRole = new UserRole();
		userRole.setRoleId(rs.getLong("UserRoleID"));
		userRole.setRoleId(rs.getLong("UserID"));
		userRole.setRoleId(rs.getLong("RoleID"));
		userRole.setDateCreated(new DateTime(rs.getTimestamp("DateCreated")));
		userRole.setDateModified(new DateTime(rs.getTimestamp("DatedModified"))); // Misspelled
		userRole.setModifiedBy(rs.getString("ModifiedBy"));

		return userRole;
	}
}
