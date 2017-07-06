package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.Application;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class ApplicationDAOImpl implements ApplicationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<Application> findAll() {
		List<Application> list = jdbcTemplate.query(Queries.FINDALLAPPLICATIONS, new ApplicationRowMapper());

		LOGGER.debug("ApplicationDAOImpl :: findAll() : Number of rows: " + list.size());

		return list;
	}
}

class ApplicationRowMapper implements RowMapper<Application> {

	@Override
	public Application mapRow(ResultSet rs, int row) throws SQLException {
		Application application = new Application();
		application.setApplicationId(rs.getLong("ApplicationID"));
		application.setApplicationName(rs.getString("ApplicationName"));

		return application;
	}
}
