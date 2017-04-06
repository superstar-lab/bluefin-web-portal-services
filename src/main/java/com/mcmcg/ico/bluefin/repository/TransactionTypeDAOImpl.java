package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class TransactionTypeDAOImpl implements TransactionTypeDAO {

	private static Logger LOGGER = LoggerFactory.getLogger(TransactionTypeDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<TransactionType> findAll() {
		List<TransactionType> list = jdbcTemplate.query(Queries.findAllTransactionTypes,
				new TransactionTypeRowMapper());

		LOGGER.debug("Number of rows: " + list.size());

		return list;
	}

	@Override
	public TransactionType findByTransactionId(long transactionTypeId) {
		TransactionType transactionType = null;

		ArrayList<TransactionType> list = (ArrayList<TransactionType>) jdbcTemplate.query(
				Queries.findTransactionTypeByTransactionId, new Object[] { transactionTypeId },
				new RowMapperResultSetExtractor<TransactionType>(new TransactionTypeRowMapper()));
		transactionType = DataAccessUtils.singleResult(list);

		if (transactionType != null) {
			LOGGER.debug("Found TransactionType for transactionTypeId: " + transactionTypeId);
		} else {
			LOGGER.debug("TransactionType not found for transactionTypeId: " + transactionTypeId);
		}

		return transactionType;
	}

	@Override
	public TransactionType findByTransactionType(String type) {
		TransactionType transactionType = null;

		ArrayList<TransactionType> list = (ArrayList<TransactionType>) jdbcTemplate.query(
				Queries.findTransactionTypeByTransactionType, new Object[] { type },
				new RowMapperResultSetExtractor<TransactionType>(new TransactionTypeRowMapper()));
		transactionType = DataAccessUtils.singleResult(list);

		if (transactionType != null) {
			LOGGER.debug("Found TransactionType for type: " + type);
		} else {
			LOGGER.debug("TransactionType not found for type: " + type);
		}

		return transactionType;
	}
}

class TransactionTypeRowMapper implements RowMapper<TransactionType> {

	@Override
	public TransactionType mapRow(ResultSet rs, int row) throws SQLException {
		TransactionType transactionType = new TransactionType();
		transactionType.setTransactionTypeId(rs.getLong("TransactionTypeID"));
		transactionType.setTransactionType(rs.getString("TransactionType"));
		transactionType.setDescription(rs.getString("Description"));
		Timestamp ts = null;
		if(rs.getString("DateCreated") != null) {
			 ts = Timestamp.valueOf(rs.getString("DateCreated"));
			transactionType.setDateCreated(new DateTime(ts));
				
		}
		if(rs.getString("DatedModified") != null) {
			ts = Timestamp.valueOf(rs.getString("DatedModified"));
			transactionType.setDateModified(new DateTime(ts));
		}
		
		transactionType.setModifiedBy(rs.getString("ModifiedBy"));

		return transactionType;
	}
}