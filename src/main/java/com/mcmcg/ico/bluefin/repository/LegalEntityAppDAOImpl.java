package com.mcmcg.ico.bluefin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class LegalEntityAppDAOImpl implements LegalEntityAppDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityAppDAOImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public LegalEntityApp findByLegalEntityAppName(String legalEntityAppName) {
        try {
            return jdbcTemplate.queryForObject(Queries.findByLegalEntityAppName, new Object[] { legalEntityAppName },
                    new LegalEntityAppRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public LegalEntityApp findByLegalEntityAppId(Long legalEntityAppId) {

        try {
            return jdbcTemplate.queryForObject(Queries.findByLegalEntityAppId, new Object[] { legalEntityAppId },
                    new LegalEntityAppRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }

    }

    @Override
    public List<LegalEntityApp> findAll() {
        List<LegalEntityApp> legalEntityApps = new ArrayList<LegalEntityApp>();
        legalEntityApps = jdbcTemplate.query(Queries.findAllLegalEntityApps, new LegalEntityAppRowMapper());
        LOGGER.debug("Number of rows: " + legalEntityApps.size());

        return legalEntityApps;
    }

    @Override
    public List<LegalEntityApp> findAll(List<Long> legalEntitiesFromUser) {
        List<LegalEntityApp> legalEntityApps = new ArrayList<LegalEntityApp>();
        legalEntityApps = jdbcTemplate.query(Queries.findAllLegalEntityAppsByIds,
                new Object[] { legalEntitiesFromUser.toString().replace("[", "").replace("]", "") },
                new LegalEntityAppRowMapper());
        LOGGER.debug("Number of rows: " + legalEntityApps.size());

        return legalEntityApps;
    }

    @Override
    public LegalEntityApp saveLegalEntityApp(LegalEntityApp legalEntityApp, String modifiedBy) {
        KeyHolder holder = new GeneratedKeyHolder();

        jdbcTemplate.update(new PreparedStatementCreator() {

            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(Queries.saveLegalEntityApp,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, legalEntityApp.getLegalEntityAppName());
                ps.setString(2, modifiedBy);
                ps.setShort(3, legalEntityApp.getIsActive());
                return ps;
            }
        }, holder);

        Long id = holder.getKey().longValue();
        legalEntityApp.setLegalEntityAppId(id);
        LOGGER.info("Created legalEntityAppId: " + id);

        return legalEntityApp;
    }

    @Override
    public void deleteLegalEntityApp(LegalEntityApp legalEntityApp) {
        jdbcTemplate.update(Queries.deleteLegalEntityApp, legalEntityApp.getLegalEntityAppId());
    }

    @Override
    public LegalEntityApp updateLegalEntityApp(LegalEntityApp legalEntityApp, String modifiedBy) {
        KeyHolder holder = new GeneratedKeyHolder();

        jdbcTemplate.update(new PreparedStatementCreator() {

            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(Queries.updateLegalEntityApp,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, legalEntityApp.getLegalEntityAppName());
                ps.setString(2, modifiedBy);
                ps.setLong(3, legalEntityApp.getLegalEntityAppId());
                return ps;
            }
        }, holder);

        LOGGER.info("Updated legalEntityAppId: " + legalEntityApp.getLegalEntityAppId());

        return legalEntityApp;
    }

}

class LegalEntityAppRowMapper implements RowMapper<LegalEntityApp> {

    @Override
    public LegalEntityApp mapRow(ResultSet rs, int row) throws SQLException {
        LegalEntityApp legalEntityApp = new LegalEntityApp();
        legalEntityApp.setLegalEntityAppId(rs.getLong("LegalEntityAppID"));
        legalEntityApp.setLegalEntityAppName(rs.getString("LegalEntityAppName"));
        legalEntityApp.setIsActive(rs.getShort("IsActive"));
        return legalEntityApp;
    }
}