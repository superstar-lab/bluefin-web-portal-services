package com.mcmcg.ico.bluefin.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
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
	public List<BinDBDetails> fetchBinDBDetailsForCardNumbers(List<String> cardNumbers) {
		LOGGER.info("Fetching all records of bin db count");
		Map<String,List<String>> cardNumbersMap = new HashMap<>();
		cardNumbersMap.put("bins", cardNumbers);
		List<BinDBDetails> binDBDetails = jdbcTemplate.query(Queries.FETCHBINDBFORBINS_MULTIPLE, cardNumbersMap, new BeanPropertyRowMapper<BinDBDetails>(BinDBDetails.class));
		if (!binDBDetails.isEmpty()) {
			binDBDetails.forEach(BinDBDetails::updateNullValuesToBlank);
		}
		return binDBDetails;
	}
	
	@Override
	public BinDBDetails fetchBinDBDetailForCardNumber(String cardFirst6Char){
		Map<String,String> cardNumbersMap = new HashMap<>();
		cardNumbersMap.put("bin", cardFirst6Char);
		List<BinDBDetails> allRecords = jdbcTemplate.query(Queries.FETCHBINDBFORBINS_SINGLE, cardNumbersMap, new BeanPropertyRowMapper<BinDBDetails>(BinDBDetails.class));
		if (!allRecords.isEmpty()) {
			BinDBDetails binDBDetails = DataAccessUtils.singleResult(allRecords);
			if (binDBDetails != null) {
				binDBDetails.updateNullValuesToBlank();
				return binDBDetails;
			}
		}
		return null;
	}

}
