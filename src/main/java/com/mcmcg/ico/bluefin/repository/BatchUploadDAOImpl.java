package com.mcmcg.ico.bluefin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.BatchUpload;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class BatchUploadDAOImpl implements BatchUploadDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchUploadDAOImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public BatchUpload saveBasicBatchUpload(BatchUpload batchUpload) {
    	LOGGER.info("Entering BatchUploadDAOImpl :: saveBasicBatchUpload()");
        KeyHolder holder = new GeneratedKeyHolder();

        DateTime utc1 = batchUpload.getProcessStart().withZone(DateTimeZone.UTC);
        DateTime utc2 = batchUpload.getDateUploaded().withZone(DateTimeZone.UTC);
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
        Timestamp processStart = Timestamp.valueOf(dtf.print(utc1));
        Timestamp dateUploaded = Timestamp.valueOf(dtf.print(utc2));

        jdbcTemplate.update(new PreparedStatementCreator() {
        	@Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(Queries.saveBasicBatchUpload,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, batchUpload.getBatchApplication());
                ps.setString(2, batchUpload.getName());
                ps.setString(3, batchUpload.getFileName());
                ps.setTimestamp(4, dateUploaded);
                ps.setString(5, batchUpload.getUpLoadedBy());
                ps.setTimestamp(6, processStart);
                ps.setInt(7, batchUpload.getNumberOfTransactions());

                return ps;
            }
        }, holder);

        Long id = holder.getKey().longValue();
        batchUpload.setBatchUploadId(id);
        LOGGER.debug("Created batchUploadId: " + id);
        LOGGER.info("Exit from BatchUploadDAOImpl :: saveBasicBatchUpload()");
        return batchUpload;
    }

    @Override
    public List<BatchUpload> findAll() {
        List<BatchUpload> batchUploads = jdbcTemplate.query(Queries.findAllBatchUploads, new BatchUploadRowMapper());
        if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug("findAll() : Number of rows: {}",( batchUploads != null ? batchUploads.size() : 0 ) );
        }
        return batchUploads;
    }

    @Override
    public BatchUpload findOne(Long id) {
        try {
        	BatchUpload batchUpload = jdbcTemplate.queryForObject(Queries.findOneBatchUpload, new Object[] { id },
                    new BatchUploadRowMapper());
        	if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("findOne() : BatchUpload found as : {} ", batchUpload);
        	}
            return batchUpload;
        } catch (EmptyResultDataAccessException e) {
        	if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for batch upload id= {}",id,e);
        	}
            return null;
        }
    }

    public static DateTime getItemDate(String date, String pattern) {
        try {
            return DateTimeFormat.forPattern(pattern).parseDateTime(date).withZone(DateTimeZone.UTC);
        } catch (Exception e) {
        	if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("Failed to convert item date",e);
        	}
            return null;
        }
    }

    @Override
    public List<BatchUpload> findByDateUploadedAfter(DateTime dateBeforeNoofdays) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
        Timestamp dateBeforeNoofdaysTimestamp = Timestamp.valueOf(dtf.print(dateBeforeNoofdays));

        List<BatchUpload> batchUploads = jdbcTemplate.query(Queries.findByDateUploadedAfter,
                new Object[] { dateBeforeNoofdaysTimestamp }, new BatchUploadRowMapper());
        LOGGER.debug("BatchUploadDAOImpl :: findByDateUploadedAfter() : Number of rows: " + batchUploads.size());
        return batchUploads;
    }

    @Override
    public Page<BatchUpload> findByDateUploadedAfterOrderByDateUploadedDesc(DateTime dateBeforeNoofdays,
            Pageable pageRequest) {
        int firstResult = (pageRequest.getPageSize() * pageRequest.getPageNumber()) + 1;
        int lastResult = firstResult + pageRequest.getPageSize();
        int batchUploadCount = jdbcTemplate.queryForObject(Queries.findCountBatchUpload, Integer.class);

        List<BatchUpload> batchUploads = jdbcTemplate.query(
                Queries.findBatchUploadsByDateUploadedAfterOrderByDateUploadedDesc,
                new Object[] { dateBeforeNoofdays, firstResult, lastResult }, new BatchUploadRowMapper());
        if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug("findByDateUploadedAfterOrderByDateUploadedDesc() : Number of rows: {}" , ( batchUploads != null ? batchUploads.size() : 0 ));
        }
        Page<BatchUpload> batchUploadsPaginated = new PageImpl(batchUploads, pageRequest,
                batchUploadCount);

        return batchUploadsPaginated;
    }

    @Override
    public Page<BatchUpload> findAllByOrderByDateUploadedDesc(PageRequest pageRequest) {
        int firstResult = (pageRequest.getPageSize() * pageRequest.getPageNumber()) + 1;
        int lastResult = firstResult + pageRequest.getPageSize();
        int batchUploadCount = jdbcTemplate.queryForObject(Queries.findCountBatchUpload, Integer.class);

        List<BatchUpload> batchUploads = jdbcTemplate.query(Queries.findAllBatchUploadsByOrderByDateUploadedDesc,
                new Object[] { firstResult, lastResult }, new BatchUploadRowMapper());
        if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug("BatchUploadDAOImpl :: findAllByOrderByDateUploadedDesc() : Number of rows: {}" + ( batchUploads != null ? batchUploads.size() : 0 ));
        }
        Page<BatchUpload> batchUploadsPaginated = new PageImpl(batchUploads, pageRequest,
                batchUploadCount);

        return batchUploadsPaginated;
    }

    class BatchUploadRowMapper implements RowMapper<BatchUpload> {

        @Override
        public BatchUpload mapRow(ResultSet rs, int row) throws SQLException {
            BatchUpload batchUpload = new BatchUpload();
            batchUpload.setBatchUploadId(rs.getLong("BatchUploadID"));
            batchUpload.setBatchApplication(rs.getString("BatchApplication"));
            batchUpload.setName(rs.getString("Name"));
            batchUpload.setFileName(rs.getString("FileName"));
            batchUpload.setDateUploaded(getItemDate(rs.getString("DateUploaded"), "YYYY-MM-dd HH:mm:ss.SSS"));
            batchUpload.setUpLoadedBy(rs.getString("UpLoadedBy"));
            batchUpload.setProcessStart(getItemDate(rs.getString("ProcessStart"), "YYYY-MM-dd HH:mm:ss.SSS"));
            batchUpload.setProcessEnd(getItemDate(rs.getString("ProcessEnd"), "YYYY-MM-dd HH:mm:ss.SSS"));
            batchUpload.setNumberOfTransactions(rs.getInt("NumberOfTransactions"));
            batchUpload.setNumberOfApprovedTransactions(rs.getInt("NumberOfApprovedTransactions"));
            batchUpload.setNumberOfDeclinedTransactions(rs.getInt("NumberOfDeclinedTransactions"));
            batchUpload.setNumberOfErrorTransactions(rs.getInt("NumberOfErrorTransactions"));
            batchUpload.setNumberOfRejected(rs.getInt("NumberOfRejected"));
            batchUpload.setNumberOfTransactionsProcessed(rs.getInt("NumberOfTransactionsProcessed"));
            return batchUpload;
        }
    }

}