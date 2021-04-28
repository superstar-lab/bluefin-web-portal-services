package com.mcmcg.ico.bluefin.repository.impl;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.mapper.TransactionRowMapper;
import com.mcmcg.ico.bluefin.model.SaleTransactionInfo;
import com.mcmcg.ico.bluefin.model.UpdateInfo;
import com.mcmcg.ico.bluefin.repository.TransactionDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Repository
@Slf4j
public class TransactionDAOImpl implements TransactionDAO {

    private JdbcTemplate jdbcTemplate;

    public TransactionDAOImpl(@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SaleTransactionInfo> getTransactionsFromUpdate(UpdateInfo update, PageRequest page) {
        log.info("TransactionDAOImpl -> getTransactionsFromUpdate. token: {}, application: {}, accountNo: {}, updateDate: {}, page: {}",
                update.getToken(), update.getApplication(), update.getAccountNo(), update.getUpdateDate(), page);
        List<SaleTransactionInfo> result = new ArrayList<>();

        try {
            Object[] params = new Object[] {
                    update.getToken(),
                    update.getApplication(),
                    update.getAccountNo(),
                    update.getUpdateDate(),
                    page.getPageSize(),
                    page.getOffset()
            };
            String query = "SELECT ApplicationTransactionID, ExpiryDate, Token, ChargeAmount, AccountId, PaymentProcessorStatusCodeDescription, TransactionDateTime, LegalEntityApp, InternalStatusCode " +
                    "FROM Sale_Transaction WHERE Token = ? AND LegalEntityApp = ? AND AccountId = ? AND TransactionDateTime > ? " +
                    "ORDER BY TransactionDateTime DESC LIMIT ? OFFSET ? ";
            result = jdbcTemplate.query(query, params, new TransactionRowMapper());
        } catch (Exception e) {
            log.error("TransactionDAOImpl -> getTransactionsFromUpdate. Error getting the transactions, token: {}, application: {}, accountNo: {}, updateDate: {}, error: {}",
                    update.getToken(), update.getApplication(), update.getAccountNo(), update.getUpdateDate(), e.toString());
        }

        return result;
    }

    @Override
    public long getTransactionsUpdateCount(UpdateInfo update) {
        log.info("TransactionDAOImpl -> getTransactionsUpdateCount. token: {}, application: {}, accountNo: {}, updateDate: {}",
                update.getToken(), update.getApplication(), update.getAccountNo(), update.getUpdateDate());
        long result = 0;

        try {
            Object[] params = new Object[] {
                    update.getToken(),
                    update.getApplication(),
                    update.getAccountNo(),
                    update.getUpdateDate()
            };
            String query = "SELECT COUNT(1) FROM Sale_Transaction WHERE Token = ? AND LegalEntityApp = ? AND AccountId = ? AND TransactionDateTime > ? ";
            result = jdbcTemplate.queryForObject(query, params, Long.class);
        } catch (Exception e) {
            log.error("TransactionDAOImpl -> getTransactionsFromUpdate. Error getting the transactions, token: {}, application: {}, accountNo: {}, updateDate: {}, error: {}",
                    update.getToken(), update.getApplication(), update.getAccountNo(), update.getUpdateDate(), e.toString());
        }

        return result;
    }

    @Override
    public List<SaleTransactionInfo> getTransactionsFromUpdates(String[] tokens) {
        log.info("TransactionDAOImpl -> getTransactionsFromUpdates. tokens: {}", Arrays.toString(tokens));
        List<SaleTransactionInfo> result = new ArrayList<>();

        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ApplicationTransactionID, ExpiryDate, Token, ChargeAmount, AccountId, PaymentProcessorStatusCodeDescription, TransactionDateTime, LegalEntityApp, InternalStatusCode " +
                    "FROM Sale_Transaction WHERE Token in ( ");
            getTokenParams(query, tokens);
            query.append(" ORDER BY TransactionDateTime");
            result = jdbcTemplate.query(query.toString(), tokens, new TransactionRowMapper());
        } catch (Exception e) {
            log.error("TransactionDAOImpl -> getTransactionsFromUpdates. Error getting the transactions, error: {}", e.toString());
        }

        return result;
    }

    private void getTokenParams(StringBuilder query, String[] tokens) {
        log.info("TransactionDAOImpl -> getTokenParams. tokens length: {}", tokens.length);

        Iterator<String> iterator = Arrays.stream(tokens).iterator();
        while (iterator.hasNext()) {
            iterator.next();
            query.append(" ? ");
            query.append(iterator.hasNext() ? ", " : ")");
        }
    }
}
