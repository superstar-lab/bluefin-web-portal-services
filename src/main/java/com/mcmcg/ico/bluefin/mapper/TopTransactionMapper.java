package com.mcmcg.ico.bluefin.mapper;

import com.mcmcg.ico.bluefin.dto.TopTranSummaryDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TopTransactionMapper implements RowMapper<TopTranSummaryDTO>{
    @Override
    public TopTranSummaryDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        TopTranSummaryDTO objTopSummary = new TopTranSummaryDTO();
        objTopSummary.setProcessor(rs.getString("Processor"));
        objTopSummary.setDeclineReason(rs.getString("DeclineReason"));
        objTopSummary.setTotalTransactions(rs.getString("TotalTransactions"));
        objTopSummary.setRate(rs.getString("Rate"));
        return objTopSummary;
    }
}

