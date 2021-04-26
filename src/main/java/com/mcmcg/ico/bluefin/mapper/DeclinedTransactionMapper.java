package com.mcmcg.ico.bluefin.mapper;

import com.mcmcg.ico.bluefin.dto.DeclinedTranSummaryDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeclinedTransactionMapper implements RowMapper<DeclinedTranSummaryDTO> {
    @Override
    public DeclinedTranSummaryDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        DeclinedTranSummaryDTO objDeclinedSummary = new DeclinedTranSummaryDTO();
        objDeclinedSummary.setProcessor(rs.getString("Processor"));
        objDeclinedSummary.setDeclined(rs.getString("Declined"));
        objDeclinedSummary.setApproved(rs.getString("Approved"));
        objDeclinedSummary.setRate(rs.getString("Rate"));
        return objDeclinedSummary;
    }
}
