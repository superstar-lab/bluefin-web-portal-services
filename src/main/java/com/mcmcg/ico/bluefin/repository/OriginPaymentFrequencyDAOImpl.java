package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.OriginPaymentFrequency;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class OriginPaymentFrequencyDAOImpl implements OriginPaymentFrequencyDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(OriginPaymentFrequencyDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<OriginPaymentFrequency> findAll() {
		List<OriginPaymentFrequency> originPaymentFrequencies = jdbcTemplate
				.query(Queries.findAllOriginPaymentFrequencies, new OriginPaymentFrequencyRowMapper() {
					public OriginPaymentFrequency mapRow(ResultSet rs, int row) throws SQLException {
						OriginPaymentFrequency originPaymentFrequency = new OriginPaymentFrequency();
						originPaymentFrequency.setOriginPaymentFrequencyId(rs.getLong("OriginPaymentFrequencyID"));
						originPaymentFrequency.setOrigin(rs.getString("Origin"));
						originPaymentFrequency.setPaymentFrequency(rs.getString("PaymentFrequency"));
						originPaymentFrequency.setDateCreated(new DateTime(rs.getTimestamp("DateCreated")));
						originPaymentFrequency.setDateModified(new DateTime(rs.getTimestamp("DateModified")));
						originPaymentFrequency.setModifiedBy(rs.getString("ModifiedBy"));
						return originPaymentFrequency;
					}
				});

		LOGGER.debug("Number of rows: " + originPaymentFrequencies.size());

		return originPaymentFrequencies;
	}
}

class OriginPaymentFrequencyRowMapper implements RowMapper<OriginPaymentFrequency> {

	@Override
	public OriginPaymentFrequency mapRow(ResultSet rs, int row) throws SQLException {
		OriginPaymentFrequency originPaymentFrequency = new OriginPaymentFrequency();
		originPaymentFrequency.setOriginPaymentFrequencyId(rs.getLong("OriginPaymentFrequencyID"));
		originPaymentFrequency.setOrigin(rs.getString("Origin"));
		originPaymentFrequency.setPaymentFrequency(rs.getString("PaymentFrequency"));
		originPaymentFrequency.setDateCreated(new DateTime(rs.getTimestamp("DateCreated")));
		originPaymentFrequency.setDateModified(new DateTime(rs.getTimestamp("DateModified")));
		originPaymentFrequency.setModifiedBy(rs.getString("ModifiedBy"));

		return originPaymentFrequency;
	}
}
