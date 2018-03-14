package com.mcmcg.ico.bluefin.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;

@Repository
public class BinDBDAOImpl implements BinDBDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BinDBDAOImpl.class);
	
	@Qualifier(BluefinWebPortalConstants.BLUEFIN_BIN_DB_JDBC_TEMPLATE)
    @Autowired
    private JdbcTemplate jdbcTemplate;
	
	@Override
	public void fetchAllBinDBs() {
		LOGGER.info("Fetching all records of bin db count");
		Integer binCount = jdbcTemplate.queryForObject("Select COUNT(distinct Bin) from BinDB_Lookup", Integer.class);
		LOGGER.info("binCount={}",binCount);
	}

}
