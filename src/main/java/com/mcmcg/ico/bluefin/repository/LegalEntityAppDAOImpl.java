package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class LegalEntityAppDAOImpl implements LegalEntityAppDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityAppDAOImpl.class);
	private final DateTimeFormatter dateCreatedDateFormat = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
	
	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public LegalEntityApp findByLegalEntityAppName(String legalEntityAppName) {
		try {
			return jdbcTemplate.queryForObject(Queries.FINDBYLEGALENTITYAPPNAME, new Object[] { legalEntityAppName },
					new LegalEntityAppRowMapper());
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for legal entity app = {}",legalEntityAppName,e);
        	}
			return null;
		}
	}

	@Override
	public LegalEntityApp findByLegalEntityAppId(Long legalEntityAppId) {
		LegalEntityApp legalEntityApp = null;
		try {
			legalEntityApp = jdbcTemplate.queryForObject(Queries.FINDBYLEGALENTITYAPPID, new Object[] { legalEntityAppId },
					new LegalEntityAppRowMapper());
			if ( LOGGER.isDebugEnabled() ) {
				LOGGER.debug("legalEntityApp: ={}", legalEntityApp);
			}
			return legalEntityApp;
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for legal entity app id = {}",legalEntityAppId,e);
        	}
			return null;
		}

	}
	
	@Override
	public LegalEntityApp findActiveLegalEntityAppId(Long legalEntityAppId) {
		LegalEntityApp legalEntityApp = null;
		try {
			legalEntityApp = jdbcTemplate.queryForObject(Queries.FINDBYLEGALENTITYAPPIDACTIVE, new Object[] { legalEntityAppId, BluefinWebPortalConstants.ACTIVELEGALENTITY },
					new LegalEntityAppRowMapper());
			if ( LOGGER.isDebugEnabled() ) {
				LOGGER.debug("legalEntityApp: ={}", legalEntityApp);
			}
			return legalEntityApp;
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for legal entity app id = {}",legalEntityAppId,e);
        	}
			return null;
		}

	}

	@Override
	public List<LegalEntityApp> findAll() {
		List<LegalEntityApp> legalEntityApps = jdbcTemplate.query(Queries.FINDALLLEGALENTITYAPPS,
				new LegalEntityAppRowMapper());

		LOGGER.debug("Number of rows ={}", legalEntityApps.size());

		return legalEntityApps;
	}

	@Override
	public List<LegalEntityApp> findAll(List<Long> legalEntityAppIds) {
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		Map<String, List<Long>> map = Collections.singletonMap("legalEntityAppIds", legalEntityAppIds);
		List<LegalEntityApp> legalEntityApps = namedParameterJdbcTemplate.query(Queries.FINDALLLEGALENTITYAPPSBYIDS,
				map, new LegalEntityAppRowMapper());

		LOGGER.debug("Number of rows ={}",legalEntityApps.size());

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
		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(connection->{
				PreparedStatement ps = connection.prepareStatement(Queries.SAVELEGALENTITYAPP,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, legalEntityApp.getLegalEntityAppName()); // LegalEntityAppName
				ps.setTimestamp(2, dateCreated); // DateCreated
				ps.setTimestamp(3, dateModified); // DateModified
				ps.setString(4, legalEntityApp.getModifiedBy()); // ModifiedBy
				ps.setShort(5, legalEntityApp.getIsActive()); // IsActive
				ps.setString(6, legalEntityApp.getPrNumber()); //PRNumber
				return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		legalEntityApp.setLegalEntityAppId(id);
		LOGGER.debug("Created legalEntityAppId ={} ", id);

		return legalEntityApp;
	}

	@Override
	public void deleteLegalEntityApp(LegalEntityApp legalEntityApp) {
		jdbcTemplate.update(Queries.DELETELEGALENTITYAPP, legalEntityApp.getLegalEntityAppId());
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
		DateTime utc2 = new DateTime(DateTimeZone.UTC);
		DateTimeFormatter dateModifiedDateFormat = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateModified = Timestamp.valueOf(dateModifiedDateFormat.print(utc2));

		jdbcTemplate.update(connection->{
				PreparedStatement ps = connection.prepareStatement(Queries.UPDATELEGALENTITYAPP,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, legalEntityApp.getLegalEntityAppName()); // LegalEntityAppName
				ps.setShort(2, legalEntityApp.getIsActive()); // IsActive
				ps.setTimestamp(3, dateModified); // DateModified
				ps.setString(4, modifiedBy); // ModifiedBy
				ps.setLong(5, legalEntityApp.getLegalEntityAppId()); // LegalEntityAppId
				ps.setString(6, legalEntityApp.getPrNumber());  //PRNumber
				
				return ps;
		}, holder);

		LOGGER.debug("Updated legalEntityAppIdv={} ", legalEntityApp.getLegalEntityAppId());

		return legalEntityApp;
	}

	@Override
	public void createLegalEntityApps(Collection<UserLegalEntityApp> legalEntities) {
		insertBatch(new ArrayList<UserLegalEntityApp>(legalEntities));
	}
	
	private void insertBatch(final List<UserLegalEntityApp> userLegalEntities){
		jdbcTemplate.batchUpdate(Queries.SAVEUSERLEGALENTITYAPP, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				UserLegalEntityApp userLegalEntity = userLegalEntities.get(i);
				DateTime utc1 = userLegalEntity.getDateCreated() != null ? userLegalEntity.getDateCreated().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
				Timestamp dateCreated = Timestamp.valueOf(dateCreatedDateFormat.print(utc1));
				LOGGER.info("Creating child item for , UserLegalEntityApp ");
				if (userLegalEntity.getUser() != null) {
					ps.setLong(1, userLegalEntity.getUser().getUserId());
				} else {
					ps.setLong(1, userLegalEntity.getUserId());
				}
				ps.setLong(2, userLegalEntity.getLegalEntityAppId());
				ps.setTimestamp(3, dateCreated);
			}
			@Override
			public int getBatchSize() {
				return userLegalEntities.size();
			}
		  });
	}
}

class LegalEntityAppRowMapper implements RowMapper<LegalEntityApp> {

	@Override
	public LegalEntityApp mapRow(ResultSet rs, int row) throws SQLException {
		Timestamp ts;
		LegalEntityApp legalEntityApp = new LegalEntityApp();
		legalEntityApp.setLegalEntityAppId(rs.getLong("LegalEntityAppID"));
		legalEntityApp.setLegalEntityAppName(rs.getString("LegalEntityAppName"));
		if(rs.getString("DateCreated") != null) {
			ts = Timestamp.valueOf(rs.getString("DateCreated"));
			legalEntityApp.setDateCreated(new DateTime(ts));
		}
		
		if(rs.getString("DatedModified") != null) {
			ts = Timestamp.valueOf(rs.getString("DatedModified"));
			legalEntityApp.setDateModified(new DateTime(ts));
		}
		legalEntityApp.setModifiedBy(rs.getString("ModifiedBy"));
		legalEntityApp.setIsActive(rs.getShort("IsActive"));

		return legalEntityApp;
	}
}