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

import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class UserLegalEntityAppDAOImpl implements UserLegalEntityAppDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserLegalEntityAppDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<UserLegalEntityApp> findByUserId(long userId) {
		ArrayList<UserLegalEntityApp> list = (ArrayList<UserLegalEntityApp>) jdbcTemplate.query(
				Queries.findUserLegalEntityAppByUserId, new Object[] { userId },
				new RowMapperResultSetExtractor<UserLegalEntityApp>(new UserLegalEntityAppRowMapper()));

		LOGGER.debug("Number of rows: " + list.size());

		return list;
	}
}

class UserLegalEntityAppRowMapper implements RowMapper<UserLegalEntityApp> {

	@Override
	public UserLegalEntityApp mapRow(ResultSet rs, int row) throws SQLException {
		UserLegalEntityApp userLegalEntityApp = new UserLegalEntityApp();
		userLegalEntityApp.setUserLegalEntityAppId(rs.getLong("UserLegalEntityAppID"));
		userLegalEntityApp.setUserId(rs.getLong("UserID"));
		userLegalEntityApp.setLegalEntityAppId(rs.getLong("LegalEntityAppID"));
		userLegalEntityApp.setDateCreated(new DateTime(rs.getTimestamp("DateCreated")));
		userLegalEntityApp.setDateModified(new DateTime(rs.getTimestamp("DatedModified"))); // Misspelled
		userLegalEntityApp.setModifiedBy(rs.getString("ModifiedBy"));

		return userLegalEntityApp;
	}
}
