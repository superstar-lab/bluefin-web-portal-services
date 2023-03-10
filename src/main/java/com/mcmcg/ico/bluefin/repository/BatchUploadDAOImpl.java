package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.BatchUpload;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.repository.sql.Queries;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;

@Repository
public class BatchUploadDAOImpl implements BatchUploadDAO {

    public static final Logger LOGGER = LoggerFactory.getLogger(BatchUploadDAOImpl.class);

    @Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
	private LegalEntityAppDAO legalEntityAppDAO;

    @Override
    public BatchUpload saveBasicBatchUpload(BatchUpload batchUpload) {
    	LOGGER.info("save Basic BatchUpload");
        KeyHolder holder = new GeneratedKeyHolder();

        DateTime utc1 = batchUpload.getProcessStart().withZone(DateTimeZone.UTC);
        DateTime utc2 = batchUpload.getDateUploaded().withZone(DateTimeZone.UTC);
        DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
        Timestamp processStart = Timestamp.valueOf(dtf.print(utc1));
        Timestamp dateUploaded = Timestamp.valueOf(dtf.print(utc2));

        jdbcTemplate.update(connection->{
                PreparedStatement ps = connection.prepareStatement(Queries.SAVEBASICBATCHUPLOAD,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, batchUpload.getBatchApplication());
                ps.setString(2, batchUpload.getName());
                ps.setString(3, batchUpload.getFileName());
                ps.setTimestamp(4, dateUploaded);
                ps.setString(5, batchUpload.getUpLoadedBy());
                ps.setTimestamp(6, processStart);
                ps.setInt(7, batchUpload.getNumberOfTransactions());
                ps.setLong(8, batchUpload.getLegalEntityAppId());
                return ps;
            }
        , holder);

        Long id = holder.getKey().longValue();
        batchUpload.setBatchUploadId(id);
        LOGGER.debug("Created batchUploadId {} ", id);
        LOGGER.info("saveBasicBatchUpload");
        return batchUpload;
    }

    @Override
    public List<BatchUpload> findAll() {
        List<BatchUpload> batchUploads = jdbcTemplate.query(Queries.FINDALLBATCHUPLOADS, new BatchUploadRowMapper());
        if (LOGGER.isDebugEnabled()) {
            int size=0;
            try {
            	size = batchUploads.size();
            }catch(Exception ex) {
            	LOGGER.error(ex.getMessage());
            }
        	LOGGER.debug("findAll() : Number of rows: {}",size);
        }
        return batchUploads;
    }

    @Override
    public BatchUpload findOne(Long id) {
        try {
        	BatchUpload batchUpload = jdbcTemplate.queryForObject(Queries.FINDONEBATCHUPLOAD, new Object[] { id },
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
        	if (date != null) {
        		return DateTimeFormat.forPattern(pattern).parseDateTime(date).withZone(DateTimeZone.UTC);
            } else {
            	LOGGER.debug("Date value to parse found null");
            }
        } catch (Exception e) {
        	if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("Failed to convert item date",e);
        	}
        }
        return null;
    }

    @Override
    public List<BatchUpload> findByDateUploadedAfter(DateTime dateBeforeNoofdays) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
        Timestamp dateBeforeNoofdaysTimestamp = Timestamp.valueOf(dtf.print(dateBeforeNoofdays));

        List<BatchUpload> batchUploads = jdbcTemplate.query(Queries.FINDBYDATEUPLOADEDAFTER,
                new Object[] { dateBeforeNoofdaysTimestamp }, new BatchUploadRowMapper());
        LOGGER.debug("Number of rows ={} ", batchUploads.size());
        return batchUploads;
    }

    @Override
    public Page<BatchUpload> findByDateUploadedAfterOrderByDateUploadedDesc(DateTime dateBeforeNoofdays,
            Pageable pageRequest) {
        int firstResult = (pageRequest.getPageSize() * pageRequest.getPageNumber()) + 1;
        int lastResult = firstResult + pageRequest.getPageSize();
        int batchUploadCount = jdbcTemplate.queryForObject(Queries.FINDCOUNTBATCHUPLOAD, Integer.class);

        List<BatchUpload> batchUploads = jdbcTemplate.query(
                Queries.FINDBATCHUPLOADSBYDATEUPLOADEDAFTERORDERBYDATEUPLOADEDDESC,
                new Object[] { dateBeforeNoofdays, firstResult, lastResult }, new BatchUploadRowMapper());
        
        if (LOGGER.isDebugEnabled()) {
            int size=0;
            try {
            	size = batchUploads.size();
            }catch(Exception ex) {
            	LOGGER.error(ex.getMessage());
            }
        	LOGGER.debug("findByDateUploadedAfterOrderByDateUploadedDesc() : Number of rows: {}" , size);
        }
        
        batchUploads = getLegalEntityNameById(batchUploads);
        
        
        return new PageImpl<BatchUpload>(batchUploads, pageRequest,
                batchUploadCount);
    }

    @Override
    public Page<BatchUpload> findAllByOrderByDateUploadedDesc(PageRequest pageRequest) {
        int firstResult = (pageRequest.getPageSize() * pageRequest.getPageNumber()) + 1;
        int lastResult = firstResult + pageRequest.getPageSize();
        int batchUploadCount = jdbcTemplate.queryForObject(Queries.FINDCOUNTBATCHUPLOAD, Integer.class);

        List<BatchUpload> batchUploads = jdbcTemplate.query(Queries.FINDALLBATCHUPLOADSBYORDERBYDATEUPLOADEDDESC,
                new Object[] { firstResult, lastResult }, new BatchUploadRowMapper());
        int size=0;
        try {
        	size = batchUploads.size();
        }catch(Exception ex) {
        	LOGGER.error(ex.getMessage());
        }
        if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug("Number of rows: ={}", size );
        }
        
        batchUploads = getLegalEntityNameById(batchUploads);
        return new PageImpl<BatchUpload>(batchUploads,pageRequest,batchUploadCount);
   
    }

    class BatchUploadRowMapper implements RowMapper<BatchUpload> {

        @Override
        public BatchUpload mapRow(ResultSet rs, int row) throws SQLException {
            BatchUpload batchUpload = new BatchUpload();
            batchUpload.setBatchUploadId(rs.getLong("BatchUploadID"));
            batchUpload.setBatchApplication(rs.getString("BatchApplication"));
            batchUpload.setName(rs.getString("Name"));
            batchUpload.setFileName(rs.getString("FileName"));
            batchUpload.setDateUploaded(getItemDate(rs.getString("DateUploaded"), BluefinWebPortalConstants.FULLDATEFORMAT));
            batchUpload.setUpLoadedBy(rs.getString("UpLoadedBy"));
            batchUpload.setProcessStart(getItemDate(rs.getString("ProcessStart"), BluefinWebPortalConstants.FULLDATEFORMAT));
            batchUpload.setProcessEnd(getItemDate(rs.getString("ProcessEnd"), BluefinWebPortalConstants.FULLDATEFORMAT));
            batchUpload.setNumberOfTransactions(rs.getInt("NumberOfTransactions"));
            batchUpload.setNumberOfApprovedTransactions(rs.getInt("NumberOfApprovedTransactions"));
            batchUpload.setNumberOfDeclinedTransactions(rs.getInt("NumberOfDeclinedTransactions"));
            batchUpload.setNumberOfErrorTransactions(rs.getInt("NumberOfErrorTransactions"));
            batchUpload.setNumberOfRejected(rs.getInt("NumberOfRejected"));
            batchUpload.setNumberOfTransactionsProcessed(rs.getInt("NumberOfTransactionsProcessed"));
            batchUpload.setLegalEntityAppId(rs.getLong("LegalEntityAppID"));
            return batchUpload;
        }
    }

    public List<BatchUpload> getLegalEntityNameById(List<BatchUpload> batchUploads) {
    	
    	for(BatchUpload batchUpload : batchUploads) {
        	LegalEntityApp legalEntityApp = legalEntityAppDAO.findByLegalEntityAppId(batchUpload.getLegalEntityAppId());
    		if (legalEntityApp == null) {
    			String message= LoggingUtil.adminAuditInfo("Legal Entity name not found at batch upload dao", BluefinWebPortalConstants.SEPARATOR,"Unable to find Legal Entity with Id at batch upload dao : ", String.valueOf(batchUpload.getLegalEntityAppId()));
    			LOGGER.debug(message);
    			batchUpload.setLegalEntityName("Not Available");
    		}
    		else {
    			batchUpload.setLegalEntityName(legalEntityApp.getLegalEntityAppName());
    		}
        }
    	
    	return batchUploads;
    }
}