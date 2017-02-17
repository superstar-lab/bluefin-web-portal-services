package com.mcmcg.ico.bluefin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class LegalEntityAppDAOImpl implements LegalEntityAppDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityAppDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public LegalEntityApp findByLegalEntityAppName(String legalEntityAppName) {
		try {
			return jdbcTemplate.queryForObject(Queries.findByLegalEntityAppName, new Object[] { legalEntityAppName },
					new LegalEntityAppRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public LegalEntityApp findByLegalEntityAppId(Long legalEntityAppId) {

		try {
			return jdbcTemplate.queryForObject(Queries.findByLegalEntityAppId, new Object[] { legalEntityAppId },
					new LegalEntityAppRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	@Override
	public List<LegalEntityApp> findAll() {
		List<LegalEntityApp> legalEntityApps = jdbcTemplate.query(Queries.findAllLegalEntityApps,
				new LegalEntityAppRowMapper());

		LOGGER.debug("Number of rows: " + legalEntityApps.size());

		return legalEntityApps;
	}

	@Override
	public List<LegalEntityApp> findAll(List<Long> legalEntityAppIds) {
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		Map<String, List<Long>> map = Collections.singletonMap("legalEntityAppIds", legalEntityAppIds);
		List<LegalEntityApp> legalEntityApps = namedParameterJdbcTemplate.query(Queries.findAllLegalEntityAppsByIds,
				map, new LegalEntityAppRowMapper());

		LOGGER.debug("Number of rows: " + legalEntityApps.size());

		return legalEntityApps;
	}

	@Override
	public LegalEntityApp saveLegalEntityApp(LegalEntityApp legalEntityApp, String modifiedBy) {
		KeyHolder holder = new GeneratedKeyHolder();

		// The Java class uses Joda DateTime, which isn't supported by
		// PreparedStatement.
		// Convert Joda DateTime to UTC (the format for the database).
		// Remove the 'T' and 'Z' from the format, because it's not in the
		// database.
		// Convert this string to Timestamp, which is supported by
		// PreparedStatement.
		DateTime utc1 = legalEntityApp.getDateCreated().withZone(DateTimeZone.UTC);
		DateTime utc2 = legalEntityApp.getDateModified().withZone(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(new PreparedStatementCreator() {

			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(Queries.saveLegalEntityApp,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, legalEntityApp.getLegalEntityAppName()); // LegalEntityAppName
				ps.setTimestamp(2, dateCreated); // DateCreated
				ps.setTimestamp(3, dateModified); // DateModified
				ps.setString(4, legalEntityApp.getModifiedBy()); // ModifiedBy
				ps.setShort(5, legalEntityApp.getIsActive()); // IsActive
				return ps;
			}
		}, holder);

		Long id = holder.getKey().longValue();
		legalEntityApp.setLegalEntityAppId(id);
		LOGGER.info("Created legalEntityAppId: " + id);

		return legalEntityApp;
	}

	@Override
	public void deleteLegalEntityApp(LegalEntityApp legalEntityApp) {
		jdbcTemplate.update(Queries.deleteLegalEntityApp, legalEntityApp.getLegalEntityAppId());
	}

	@Override
	public LegalEntityApp updateLegalEntityApp(LegalEntityApp legalEntityApp, String modifiedBy) {
		KeyHolder holder = new GeneratedKeyHolder();

		// The Java class uses Joda DateTime, which isn't supported by
		// PreparedStatement.
		// Convert Joda DateTime to UTC (the format for the database).
		// Remove the 'T' and 'Z' from the format, because it's not in the
		// database.
		// Convert this string to Timestamp, which is supported by
		// PreparedStatement.
		DateTime utc1 = legalEntityApp.getDateCreated().withZone(DateTimeZone.UTC);
		DateTime utc2 = new DateTime(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(new PreparedStatementCreator() {

			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(Queries.updateLegalEntityApp,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, legalEntityApp.getLegalEntityAppName()); // LegalEntityAppName
				ps.setTimestamp(2, dateCreated); // DateCreated
				ps.setTimestamp(3, dateModified); // DateModified
				ps.setString(4, modifiedBy); // ModifiedBy
				ps.setShort(5, legalEntityApp.getIsActive()); // IsActive
				return ps;
			}
		}, holder);

		LOGGER.info("Updated legalEntityAppId: " + legalEntityApp.getLegalEntityAppId());

		return legalEntityApp;
	}
}

class LegalEntityAppRowMapper implements RowMapper<LegalEntityApp> {

	@Override
	public LegalEntityApp mapRow(ResultSet rs, int row) throws SQLException {
		LegalEntityApp legalEntityApp = new LegalEntityApp();
		legalEntityApp.setLegalEntityAppId(rs.getLong("LegalEntityAppID"));
		legalEntityApp.setLegalEntityAppName(rs.getString("LegalEntityAppName"));
		legalEntityApp.setDateCreated(new DateTime(rs.getTimestamp("DateCreated")));
		legalEntityApp.setDateModified(new DateTime(rs.getTimestamp("DatedModified"))); // Misspelled
		// in
		// database!
		legalEntityApp.setModifiedBy(rs.getString("ModifiedBy"));
		legalEntityApp.setIsActive(rs.getShort("IsActive"));

		return legalEntityApp;
	}
}