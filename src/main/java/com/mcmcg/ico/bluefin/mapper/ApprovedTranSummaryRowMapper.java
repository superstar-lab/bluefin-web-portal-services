package com.mcmcg.ico.bluefin.mapper;

import com.mcmcg.ico.bluefin.model.ApprovedTranSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class ApprovedTranSummaryRowMapper implements RowMapper<ApprovedTranSummary> {

	@Override
	public ApprovedTranSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
		ApprovedTranSummary approvedTranSummary = new ApprovedTranSummary();
		approvedTranSummary.setLegalEntity(rs.getString("LegalEntityApp"));
		approvedTranSummary.setNumberTransactions(rs.getString("Transactions"));
		approvedTranSummary.setTotalAmount(rs.getString("TotalAmount"));
		approvedTranSummary.setProcessor(rs.getString("Processor"));
		return approvedTranSummary;
	}
}
