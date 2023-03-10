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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.ReconciliationStatus;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class ReconciliationStatusDAOImpl implements ReconciliationStatusDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReconciliationStatusDAOImpl.class);

	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<ReconciliationStatus> findAll() {
		List<ReconciliationStatus> list;
		try {
		list = jdbcTemplate.query(Queries.FINDALLRECONCILIATIONSTATUSES,
				new ReconciliationStatusRowMapper());
		LOGGER.debug("Number of rows ={} ",list.size());
		return list;
		}catch(Exception ex) {
			LOGGER.error("fetchInternalResponseCodeIdsMappedForPaymentProcessorResponseCodeIds idsFetched cannot be NULL {}",ex.getMessage());
			list = new ArrayList<ReconciliationStatus>();
		}
		return list;
	}

	@Override
	public ReconciliationStatus findByReconciliationStatusId(long reconciliationStatusId) {
		ArrayList<ReconciliationStatus> list = (ArrayList<ReconciliationStatus>) jdbcTemplate.query(
				Queries.FINDRECONCILIATIONSTATUSBYRECONCILIATIONSTATUSID, new Object[] { reconciliationStatusId },
				new RowMapperResultSetExtractor<ReconciliationStatus>(new ReconciliationStatusRowMapper()));
		ReconciliationStatus reconciliationStatus = DataAccessUtils.singleResult(list);

		if (reconciliationStatus != null) {
			LOGGER.debug("Found ReconciliationStatus for reconciliationStatusId ={} ", reconciliationStatusId);
		} else {
			LOGGER.debug("ReconciliationStatus not found for reconciliationStatusId ={} ", reconciliationStatusId);
		}

		return reconciliationStatus;
	}

	@Override
	public ReconciliationStatus findByReconciliationStatus(String status) {
		ArrayList<ReconciliationStatus> list = (ArrayList<ReconciliationStatus>) jdbcTemplate.query(
				Queries.FINDRECONCILIATIONSTATUSBYRECONCILIATIONSTATUS, new Object[] { status },
				new RowMapperResultSetExtractor<ReconciliationStatus>(new ReconciliationStatusRowMapper()));
		LOGGER.debug("ReconciliationStatus size = {} ",list.size());
		ReconciliationStatus reconciliationStatus = DataAccessUtils.singleResult(list);

		if (reconciliationStatus != null) {
			LOGGER.debug("Found ReconciliationStatus for reconciliationStatus ={} ", status);
		} else {
			LOGGER.debug("ReconciliationStatus not found for reconciliationStatus = {} ", status);
		}

		return reconciliationStatus;
	}
}

class ReconciliationStatusRowMapper implements RowMapper<ReconciliationStatus> {

	@Override
	public ReconciliationStatus mapRow(ResultSet rs, int row) throws SQLException {
		ReconciliationStatus reconciliationStatus = new ReconciliationStatus();
		reconciliationStatus.setReconciliationStatusId(rs.getLong("ReconciliationStatusID"));
		reconciliationStatus.setReconciliationStatusValue(rs.getString("ReconciliationStatus"));
		reconciliationStatus.setDescription(rs.getString("Description"));
		Timestamp ts;
		if (rs.getString("DateCreated") != null) {

			ts = Timestamp.valueOf(rs.getString("DateCreated"));
			reconciliationStatus.setDateCreated(new DateTime(ts));	
		}
		if (rs.getString("DateModified") != null) {

			ts = Timestamp.valueOf(rs.getString("DateModified"));
			reconciliationStatus.setDateModified(new DateTime(ts));	
		}
		reconciliationStatus.setModifiedBy(rs.getString("ModifiedBy"));

		return reconciliationStatus;
	}
}
