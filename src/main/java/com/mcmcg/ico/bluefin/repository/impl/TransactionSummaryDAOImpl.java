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
    public List<DeclinedTranSummaryDTO> declinedSummary(String fromDatetime, String toDatetime, String statusCode) {
        log.info("TransactionSummaryDAOImpl -> declinedSummary, fromDate: {}, toDatetime : {} , statusCode : {}", fromDatetime, toDatetime, statusCode);
        List<DeclinedTranSummaryDTO> result = new ArrayList<>();
        try {
            result = jdbcTemplate.query("CALL spTransactionStatusMetrics(?,?,?)", new Object[]{ fromDatetime, toDatetime, statusCode }, new DeclinedTransactionMapper());
        } catch (Exception ex) {
            log.error("TransactionSummaryDAOImpl -> declinedSummary, Error while retrieving declined transactions. fromDate: {}, toDatetime : {}, statusCode : {} , Error: {}", fromDatetime, toDatetime, statusCode, ex.getMessage());
        }
        return result;
    }

    @Override
    public List<ApprovedTranSummary> approvedSummary(String fromDatetime, String toDatetime) {
        log.info("TransactionSummaryDAOImpl -> approvedSummary, fromDatetime: {}, toDatetime : {}", fromDatetime, toDatetime);
        List<ApprovedTranSummary> result = new ArrayList<>();
        try {
            result = jdbcTemplate.query("CALL spGetApprovedTransactionSummary(?,?)", new Object[]{ fromDatetime, toDatetime }, new ApprovedTranSummaryRowMapper());
        } catch (Exception ex) {
            log.error("TransactionSummaryDAOImpl -> approvedSummary, Error while retrieving approved transactions. fromDatetime: {}, toDatetime : {}, Error: {}", fromDatetime, toDatetime, ex.getMessage());
        }
        return result;
    }

    public List<TopTranSummaryDTO> topSummary(String top, String statusCode, String fromDatetime, String toDatetime) {
        log.info("TransactionSummaryDAOImpl -> topSummary, top : {},statusCode : {}, fromDate: {}, toDatetime : {}", top, statusCode, fromDatetime, toDatetime);
        List<TopTranSummaryDTO> result = new ArrayList<>();
        try {
            result = jdbcTemplate.query("CALL spTopTransactionStatus(?,?,?,?)", new Object[]{ top, statusCode, fromDatetime, toDatetime }, new TopTransactionMapper());
        } catch (Exception ex) {
            log.error("TransactionSummaryDAOImpl -> topSummary, Error while retrieving top summary transactions. top : {},statusCode : {}, fromDate: {}, toDatetime : {}, Error: {}", top, statusCode, fromDatetime, toDatetime, ex.getMessage());
        }
        return result;
    }
}
