package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.mapper.AccountValidationRowMapper;
import com.mcmcg.ico.bluefin.model.AccountValidation;
import com.mcmcg.ico.bluefin.repository.sql.Queries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AccountValidationDAOImpl implements AccountValidationDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountValidationDAOImpl.class);

    @Qualifier(BluefinWebPortalConstants.BLUEFIN_ACCOUNT_VALIDATION_JDBC_TEMPLATE)
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<AccountValidation> findAll() {
        return jdbcTemplate.query(Queries.ACCOUNT_VALIDATION, new AccountValidationRowMapper());
    }

    @Override
    public Map<String, Object> findAllFilter(String startDate, String endDate, PageRequest page) {
        LOGGER.info("Fetching Account Validation Filtering, Search  startDate {}, endDate {}, page {} ", startDate, endDate, page);

        int pageNumber = page != null ? page.getPageNumber() : 0;
        int pageSize = page != null ? page.getPageSize() : 0;

        List<AccountValidation> list = jdbcTemplate.query(Queries.ACCOUNT_VALIDATION_FILTER_PAGE, new Object[]{startDate, endDate, (pageNumber * pageSize), pageSize}, new AccountValidationRowMapper());
        List<AccountValidation> totalList = jdbcTemplate.query(Queries.ACCOUNT_VALIDATION_FILTER_ALL, new Object[]{startDate, endDate}, new AccountValidationRowMapper());

        Map<String, Object> map = new HashMap<>();
        map.put("totalCounts", totalList.size());
        map.put("contents", list);
        map.put("totalContents", totalList);

        return map;
    }
}
