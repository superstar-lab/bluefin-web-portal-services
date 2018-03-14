package com.mcmcg.ico.bluefin.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.BinDBDetails;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class BinDBDAOImpl implements BinDBDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BinDBDAOImpl.class);
	
	@Qualifier(BluefinWebPortalConstants.BLUEFIN_BIN_DB_JDBC_TEMPLATE)
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
	
	@Override
	public List<BinDBDetails> fetchDetailsForCardNumbers(List<String> cardNumbers) {
		LOGGER.info("Fetching all records of bin db count");
		Map<String,List<String>> cardNumbersMap = new HashMap<>();
		cardNumbersMap.put("bins", cardNumbers);
		return jdbcTemplate.query(Queries.FETCHBINDBFORBINS, cardNumbersMap, new BeanPropertyRowMapper(BinDBDetails.class));
	}

}
