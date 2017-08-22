package com.mcmcg.ico.bluefin.repository;

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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode;
import com.mcmcg.ico.bluefin.model.Permission;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorInternalStatusCodeDAOImpl.PaymentProcessorInternalStatusCodeRowMapper;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class PermissionDAOImpl implements PermissionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PermissionDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public Permission findByPermissionId(long permissionId) {
		Permission permission;

		ArrayList<Permission> list = (ArrayList<Permission>) jdbcTemplate.query(Queries.FINDPERMISSIONBYPERMISSIONID,
				new Object[] { permissionId }, new RowMapperResultSetExtractor<Permission>(new PermissionRowMapper()));
		LOGGER.debug("Permission list ={} ", list.size());
		permission = DataAccessUtils.singleResult(list);

		if (permission != null) {
			LOGGER.debug("Found Permission for permissionId ={} ", permissionId);
		} else {
			LOGGER.debug("Permission not found for permissionId ={} ", permissionId);
		}

		return permission;
	}

	@Override
	public Permission findByPermissionName(String permissionName) {
		Permission permission;
		ArrayList<Permission> list = (ArrayList<Permission>) jdbcTemplate.query(Queries.FINDPERMISSIONBYPERMISSIONNAME,
				new Object[] { permissionName },
				new RowMapperResultSetExtractor<Permission>(new PermissionRowMapper()));
		LOGGER.debug("Permission list ={} ", list.size());
		permission = DataAccessUtils.singleResult(list);

		if (permission != null) {
			LOGGER.debug("Found Permission for permissionName ={} ", permissionName);
		} else {
			LOGGER.debug("Permission not found for permissionName ={} ", permissionName);
		}

		return permission;
	}

	@Override
	public long savePermission(Permission permission) {
		KeyHolder holder = new GeneratedKeyHolder();

		// The Java class uses Joda DateTime, which isn't supported by
		// PreparedStatement.
		// Convert Joda DateTime to UTC (the format for the database).
		// Remove the 'T' and 'Z' from the format, because it's not in the
		// database.
		// Convert this string to Timestamp, which is supported by
		// PreparedStatement.
		DateTime utc1 = permission.getDateCreated().withZone(DateTimeZone.UTC);
		DateTime utc2 = permission.getDateModified().withZone(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(connection->{
				PreparedStatement ps = connection.prepareStatement(Queries.SAVEPERMISSION,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, permission.getPermissionName()); // PermissionName
				ps.setString(2, permission.getDescription()); // Description
				ps.setTimestamp(3, dateCreated); // DateCreated
				ps.setTimestamp(4, dateModified); // DateModified
				ps.setString(5, permission.getModifiedBy()); // ModifiedBy
				return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		permission.setPermissionId(id);
		LOGGER.debug("Saved permission - id ={} ", id);

		return id;
	}

	@Override
	public List<Permission> findByRoleId(Long roleId) {
		List<Permission> list = jdbcTemplate.query( Queries.FINDPERMISSIONBYROLEID, new Object[] {roleId},
				new PermissionRowMapper());
		return list;
	}
}

class PermissionRowMapper implements RowMapper<Permission> {

	@Override
	public Permission mapRow(ResultSet rs, int row) throws SQLException {
		Permission permission = new Permission();
		permission.setPermissionId(rs.getLong("PermissionID"));
		permission.setPermissionName(rs.getString("PermissionName"));
		permission.setDescription(rs.getString("Description"));
		Timestamp ts;
		if(rs.getString("DateCreated") != null) {
			ts = Timestamp.valueOf(rs.getString("DateCreated"));
			permission.setDateCreated(new DateTime(ts));
		}
		if(rs.getString("DatedModified") != null) {
			ts = Timestamp.valueOf(rs.getString("DatedModified"));
			permission.setDateModified(new DateTime(ts));
		}
	
		permission.setModifiedBy(rs.getString("ModifiedBy"));

		return permission;
	}
}
