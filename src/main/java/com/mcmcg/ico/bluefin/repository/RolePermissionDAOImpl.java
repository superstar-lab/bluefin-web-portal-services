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

import com.mcmcg.ico.bluefin.model.RolePermission;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class RolePermissionDAOImpl implements RolePermissionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(RolePermissionDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<RolePermission> findByRoleId(long roleId) {
		ArrayList<RolePermission> list = (ArrayList<RolePermission>) jdbcTemplate.query(
				Queries.findRolePermissionByRoleId, new Object[] { roleId },
				new RowMapperResultSetExtractor<RolePermission>(new RolePermissionRowMapper()));

		LOGGER.debug("RolePermissionDAOImpl :: findByRoleId() : Number of rows: " + list.size());

		return list;
	}

	@Override
	public long saveRolePermission(RolePermission rolePermission) {
		KeyHolder holder = new GeneratedKeyHolder();

		// The Java class uses Joda DateTime, which isn't supported by
		// PreparedStatement.
		// Convert Joda DateTime to UTC (the format for the database).
		// Remove the 'T' and 'Z' from the format, because it's not in the
		// database.
		// Convert this string to Timestamp, which is supported by
		// PreparedStatement.
		DateTime utc1 = rolePermission.getDateCreated().withZone(DateTimeZone.UTC);
		DateTime utc2 = rolePermission.getDateModified().withZone(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(Queries.saveRolePermission,
						Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, rolePermission.getRoleId()); // RoleID
				ps.setLong(1, rolePermission.getPermissionId()); // PermissionID
				ps.setTimestamp(3, dateCreated); // DateCreated
				ps.setTimestamp(4, dateModified); // DateModified
				ps.setString(5, rolePermission.getModifiedBy()); // ModifiedBy
				return ps;
			}
		}, holder);

		Long id = holder.getKey().longValue();
		rolePermission.setRolePermissionId(id);
		LOGGER.debug("RolePermissionDAOImpl :: saveRolePermission() : Saved rolePermission - id: " + id);

		return id;
	}
}

class RolePermissionRowMapper implements RowMapper<RolePermission> {

	@Override
	public RolePermission mapRow(ResultSet rs, int row) throws SQLException {
		RolePermission rolePermission = new RolePermission();
		rolePermission.setRolePermissionId(rs.getLong("RolePermissionID"));
		rolePermission.setRoleId(rs.getLong("RoleID"));
		rolePermission.setPermissionId(rs.getLong("PermissionID"));
		Timestamp ts;
		if (rs.getString("DateCreated") != null) {
			ts = Timestamp.valueOf(rs.getString("DateCreated"));
			rolePermission.setDateCreated(new DateTime(ts));
		}
		
		if (rs.getString("DatedModified") != null) {

			ts = Timestamp.valueOf(rs.getString("DatedModified"));
			rolePermission.setDateModified(new DateTime(ts));
		}
		rolePermission.setModifiedBy(rs.getString("ModifiedBy"));

		return rolePermission;
	}
}