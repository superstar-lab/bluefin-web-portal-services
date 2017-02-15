package com.mcmcg.ico.bluefin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class RoleDAOImpl implements RoleDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(RoleDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<Role> findAll() {
		List<Role> list = jdbcTemplate.query(Queries.findAllRoles, new RoleRowMapper());

		LOGGER.info("Number of rows: " + list.size());

		return list;
	}

	@Override
	public Role findByRoleId(long roleId) {
		Role role = null;

		ArrayList<Role> list = (ArrayList<Role>) jdbcTemplate.query(Queries.findRoleByRoleId, new Object[] { roleId },
				new RowMapperResultSetExtractor<Role>(new RoleRowMapper()));
		role = DataAccessUtils.singleResult(list);

		if (role != null) {
			LOGGER.info("Found Role for roleId: " + roleId);
		} else {
			LOGGER.info("Role not found for roleId: " + roleId);
		}

		return role;
	}

	@Override
	public Role findByRoleName(String roleName) {
		Role role = null;

		ArrayList<Role> list = (ArrayList<Role>) jdbcTemplate.query(Queries.findRoleByRoleName,
				new Object[] { roleName }, new RowMapperResultSetExtractor<Role>(new RoleRowMapper()));
		role = DataAccessUtils.singleResult(list);

		if (role != null) {
			LOGGER.info("Found Role for roleName: " + roleName);
		} else {
			LOGGER.info("Role not found for roleName: " + roleName);
		}

		return role;
	}

	@Override
	public long saveRole(Role role) {
		KeyHolder holder = new GeneratedKeyHolder();

		// The Java class uses Joda DateTime, which isn't supported by
		// PreparedStatement.
		// Convert Joda DateTime to UTC (the format for the database).
		// Remove the 'T' and 'Z' from the format, because it's not in the
		// database.
		// Convert this string to Timestamp, which is supported by
		// PreparedStatement.
		DateTime utc1 = role.getDateCreated().withZone(DateTimeZone.UTC);
		DateTime utc2 = role.getDateModified().withZone(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(new PreparedStatementCreator() {

			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(Queries.saveRole, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, role.getRoleName()); // RoleName
				ps.setString(2, role.getDescription()); // Description
				ps.setTimestamp(3, dateCreated); // DateCreated
				ps.setTimestamp(4, dateModified); // DateModified
				ps.setString(5, role.getModifiedBy()); // ModifiedBy
				return ps;
			}
		}, holder);

		Long id = holder.getKey().longValue();
		role.setRoleId(id);
		LOGGER.info("Saved role - id: " + id);

		return id;
	}

	@Override
	public int deleteByRoleName(String roleName) {

		int rows = jdbcTemplate.update(Queries.deleteRoleByRoleName, new Object[] { roleName });

		LOGGER.info("Deleted role with roleName: " + roleName + ", rows affected = " + rows);

		return rows;
	}
}

class RoleRowMapper implements RowMapper<Role> {

	@Override
	public Role mapRow(ResultSet rs, int row) throws SQLException {
		Role role = new Role();
		role.setRoleId(rs.getLong("RoleID"));
		role.setRoleName(rs.getString("RoleName"));
		role.setDescription(rs.getString("Description"));
		role.setDateCreated(new DateTime(rs.getTimestamp("DateCreated")));
		role.setDateModified(new DateTime(rs.getTimestamp("DatedModified"))); // Misspelled
																				// in
																				// database!
		role.setModifiedBy(rs.getString("ModifiedBy"));

		return role;
	}
}
