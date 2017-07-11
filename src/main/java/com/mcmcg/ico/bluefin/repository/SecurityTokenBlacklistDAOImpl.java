package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

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
import com.mcmcg.ico.bluefin.model.SecurityTokenBlacklist;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class SecurityTokenBlacklistDAOImpl implements SecurityTokenBlacklistDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityTokenBlacklistDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public SecurityTokenBlacklist findByTokenId(long tokenId) {
		ArrayList<SecurityTokenBlacklist> list = (ArrayList<SecurityTokenBlacklist>) jdbcTemplate.query(
				Queries.FINDSECURITYTOKENBLACKLISTBYTOKENID, new Object[] { tokenId },
				new RowMapperResultSetExtractor<SecurityTokenBlacklist>(new SecurityTokenBlacklistRowMapper()));
		LOGGER.debug("SecurityTokenBlacklist size ={} ",list.size());
		SecurityTokenBlacklist securityTokenBlacklist = DataAccessUtils.singleResult(list);

		if (securityTokenBlacklist != null) {
			LOGGER.debug("Found SecurityTokenBlacklist for tokenId ={} ", tokenId);
		} else {
			LOGGER.debug("SecurityTokenBlacklist not found for tokenId ={} ", tokenId);
		}

		return securityTokenBlacklist;
	}

	@Override
	public SecurityTokenBlacklist findByToken(String token) {
		ArrayList<SecurityTokenBlacklist> list = (ArrayList<SecurityTokenBlacklist>) jdbcTemplate.query(
				Queries.FINDSECURITYTOKENBLACKLISTBYTOKEN, new Object[] { token },
				new RowMapperResultSetExtractor<SecurityTokenBlacklist>(new SecurityTokenBlacklistRowMapper()));
		LOGGER.debug("SecurityTokenBlacklist of size ={} ",list.size());
		SecurityTokenBlacklist securityTokenBlacklist = DataAccessUtils.singleResult(list);

		if (securityTokenBlacklist != null) {
			LOGGER.debug("Found SecurityTokenBlacklist for token={} ", token);
		} else {
			LOGGER.debug("SecurityTokenBlacklist not found for token={} ", token);
		}

		return securityTokenBlacklist;
	}

	@Override
	public SecurityTokenBlacklist findByUserIdAndToken(long userId, String token) {
		ArrayList<SecurityTokenBlacklist> list = (ArrayList<SecurityTokenBlacklist>) jdbcTemplate.query(
				Queries.FINDSECURITYTOKENBLACKLISTBYUSERIDANDTOKEN, new Object[] { userId, token },
				new RowMapperResultSetExtractor<SecurityTokenBlacklist>(new SecurityTokenBlacklistRowMapper()));
		LOGGER.debug("size of SecurityTokenBlacklist ={} ",list.size());
		SecurityTokenBlacklist securityTokenBlacklist = DataAccessUtils.singleResult(list);

		if (securityTokenBlacklist != null) {
			LOGGER.debug("Found SecurityTokenBlacklist for userId/token ={} ", userId, "/", token);
		} else {
			LOGGER.debug("SSecurityTokenBlacklist not found for userId/token={} ", userId,  "/" , token);
		}

		return securityTokenBlacklist;
	}

	@Override
	public SecurityTokenBlacklist findByUserIdAndType(long userId, String type) {
		ArrayList<SecurityTokenBlacklist> list = (ArrayList<SecurityTokenBlacklist>) jdbcTemplate.query(
				Queries.FINDSECURITYTOKENBLACKLISTBYUSERIDANDTYPE, new Object[] { userId, type },
				new RowMapperResultSetExtractor<SecurityTokenBlacklist>(new SecurityTokenBlacklistRowMapper()));
		LOGGER.debug("Token size for security size ={} ",list.size());
		SecurityTokenBlacklist securityTokenBlacklist = DataAccessUtils.singleResult(list);

		if (securityTokenBlacklist != null) {
			LOGGER.debug("Found SecurityTokenBlacklist for userId/type ={} ", userId, "/" , type);
		} else {
			LOGGER.debug("SecurityTokenBlacklist not found for userId/type: ", userId, "/", type);
		}

		return securityTokenBlacklist;
	}

	@Override
	public long saveSecurityTokenBlacklist(SecurityTokenBlacklist securityTokenBlacklist) {
		KeyHolder holder = new GeneratedKeyHolder();

		// The Java class uses Joda DateTime, which isn't supported by
		// PreparedStatement.
		// Convert Joda DateTime to UTC (the format for the database).
		// Remove the 'T' and 'Z' from the format, because it's not in the
		// database.
		// Convert this string to Timestamp, which is supported by
		// PreparedStatement.
		DateTime utc = securityTokenBlacklist.getDateCreated().withZone(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc));

		jdbcTemplate.update(connection->{
				PreparedStatement ps = connection.prepareStatement(Queries.SAVESECURITYTOKENBLACKLIST,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, securityTokenBlacklist.getToken()); // Token
				ps.setString(2, securityTokenBlacklist.getType()); // Type
				ps.setLong(3, securityTokenBlacklist.getUserId()); // UserId
				ps.setTimestamp(4, dateCreated); // DateCreated
				return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		securityTokenBlacklist.setTokenId(id);
		LOGGER.debug("Saved securityTokenBlacklist - id ={} ", id);

		return id;
	}
}

class SecurityTokenBlacklistRowMapper implements RowMapper<SecurityTokenBlacklist> {

	@Override
	public SecurityTokenBlacklist mapRow(ResultSet rs, int row) throws SQLException {
		SecurityTokenBlacklist securityTokenBlacklist = new SecurityTokenBlacklist();
		securityTokenBlacklist.setTokenId(rs.getLong("TokenID"));
		securityTokenBlacklist.setToken(rs.getString("Token"));
		securityTokenBlacklist.setType(rs.getString("Type"));
		securityTokenBlacklist.setUserId(rs.getLong("UserID"));
		if(rs.getString("DateCreated") != null) {
			Timestamp ts = Timestamp.valueOf(rs.getString("DateCreated"));

			securityTokenBlacklist.setDateCreated(new DateTime(ts));
		}
		return securityTokenBlacklist;
	}
}
