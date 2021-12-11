package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.mapper.AccountValidationRowMapper;
import com.mcmcg.ico.bluefin.model.AccountValidation;
import com.mcmcg.ico.bluefin.repository.sql.Queries;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AccountValidationDAOImpl implements AccountValidationDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountValidationDAOImpl.class);

    @Qualifier(BluefinWebPortalConstants.BLUEFIN_ACCOUNT_VALIDATION_JDBC_TEMPLATE)
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private HashMap<String, String> predicatesHashMapping = new HashMap<>();
    private Object Integer;

    @Override
    public List<AccountValidation> findAll() {
        List<AccountValidation> list = jdbcTemplate.query(Queries.ACCOUNT_VALIDATION, new AccountValidationRowMapper());
        return list;
    }

    @Override
    public Map<String, Object> findAllFilter(String search, PageRequest page) throws ParseException {
        LOGGER.info("Fetching Account Validation Filtering, Search  Value {} , page{} ", search, page );

        int pageNumber = page != null ? page.getPageNumber() : 0;
        int pageSize = page != null ? page.getPageSize() : 0;
        String sort = page != null ? String.valueOf(page.getSort()) : "DESC";
        String ACCOUNT_VALIDATION_FILTER = "SELECT * FROM AccountValidation WHERE " + search + " ORDER BY `requestDateTime`" + " LIMIT " + pageNumber * pageSize + "," + pageSize;
        String ACCOUNT_VALIDATION_COUNTS = "SELECT * FROM AccountValidation WHERE " + search;

        List<AccountValidation> list = jdbcTemplate.query(ACCOUNT_VALIDATION_FILTER, new AccountValidationRowMapper());
        List<AccountValidation> totalList = jdbcTemplate.query(ACCOUNT_VALIDATION_COUNTS, new AccountValidationRowMapper());

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("totalCounts", totalList.size() );
        map.put("contents", list );
        map.put("totalContents", totalList );

        return map;
    }
}

