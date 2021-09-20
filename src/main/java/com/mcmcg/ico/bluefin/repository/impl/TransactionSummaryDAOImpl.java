package com.mcmcg.ico.bluefin.repository.impl;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.dto.DeclinedTranSummaryDTO;
import com.mcmcg.ico.bluefin.dto.TopTranSummaryDTO;
import com.mcmcg.ico.bluefin.mapper.ApprovedTranSummaryRowMapper;
import com.mcmcg.ico.bluefin.mapper.DeclinedTransactionMapper;
import com.mcmcg.ico.bluefin.mapper.TopTransactionMapper;
import com.mcmcg.ico.bluefin.model.ApprovedTranSummary;
import com.mcmcg.ico.bluefin.repository.TransactionSummaryDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class TransactionSummaryDAOImpl implements TransactionSummaryDAO {

    private JdbcTemplate jdbcTemplate;

    public TransactionSummaryDAOImpl(@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<DeclinedTranSummaryDTO> declinedSummary(String fromDate, String toDate, String statusCode) {
        log.info("TransactionSummaryDAOImpl -> declinedSummary, fromDate: {}, toDate : {} , statusCode : {}", fromDate, toDate, statusCode);
        List<DeclinedTranSummaryDTO> result = new ArrayList<>();
        try {
            result = jdbcTemplate.query("CALL spTransactionStatusMetrics(?,?,?)", new Object[]{ fromDate, toDate.concat(" 23:59:59"), statusCode }, new DeclinedTransactionMapper());
        } catch (Exception ex) {
            log.error("TransactionSummaryDAOImpl -> declinedSummary, Error while retrieving declined transactions. fromDate: {}, toDate : {} , statusCode : {} , Error: {}", fromDate, toDate, statusCode, ex.getMessage());
        }
        return result;
    }

    @Override
    public List<ApprovedTranSummary> approvedSummary(String fromDate, String toDate) {
        log.info("TransactionSummaryDAOImpl -> approvedSummary, fromDate: {}, toDate : {}", fromDate, toDate);
        List<ApprovedTranSummary> result = new ArrayList<>();
        try {
            result = jdbcTemplate.query("CALL spGetApprovedTransactionSummary(?,?)", new Object[]{ fromDate, toDate.concat(" 23:59:59") }, new ApprovedTranSummaryRowMapper());
        } catch (Exception ex) {
            log.error("TransactionSummaryDAOImpl -> approvedSummary, Error while retrieving approved transactions. fromDate: {}, toDate : {}, Error: {}", fromDate, toDate, ex.getMessage());
        }
        return result;
    }

    public List<TopTranSummaryDTO> topSummary(String top, String statusCode, String fromDate, String toDate) {
        log.info("TransactionSummaryDAOImpl -> topSummary, top : {},statusCode : {}, fromDate: {}, toDate : {} ", top, statusCode, fromDate, toDate);
        List<TopTranSummaryDTO> result = new ArrayList<>();
        try {
            result = jdbcTemplate.query("CALL spTopTransactionStatus(?,?,?,?)", new Object[]{ top, statusCode, fromDate, toDate.concat(" 23:59:59")  }, new TopTransactionMapper());
        } catch (Exception ex) {
            log.error("TransactionSummaryDAOImpl -> topSummary, Error while retrieving top summary transactions. top : {},statusCode : {}, fromDate: {}, toDate : {} , Error: {}", top, statusCode, fromDate, toDate, ex.getMessage());
        }
        return result;
    }
}
