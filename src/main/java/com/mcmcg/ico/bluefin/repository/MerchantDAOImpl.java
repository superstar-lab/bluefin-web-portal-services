package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.mapper.MerchantMapper;
import com.mcmcg.ico.bluefin.model.Merchant;
import com.mcmcg.ico.bluefin.model.RemittanceSale;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MerchantDAOImpl implements MerchantDAO{

    private static final Logger LOGGER = LoggerFactory.getLogger(OriginPaymentFrequencyDAOImpl.class);

    String FIND_ALL_MERCHANT_BY_PROCESSOR_ID = "select PaymentProcessorMerchantID, LegalEntityAppID, PaymentProcessorID, TestOrProd, DateCreated, DatedModified, ModifiedBy, MerchantID_Credit, MerchantID_Debit from PaymentProcessor_Merchant where PaymentProcessorID = ? ";
    String FIND_ALL_MERCHANT_EXT = "SELECT distinct (MerchantID) FROM( SELECT PPM.MerchantID_Credit AS MerchantID, PPL.ProcessorName FROM PaymentProcessor_Merchant PPM INNER JOIN PaymentProcessor_Lookup PPL ON PPM.PaymentProcessorID = PPL.PaymentProcessorID WHERE TRIM(PPM.MerchantID_Credit)<>'' AND PPM.MerchantID_Credit IS NOT NULL UNION SELECT PPM.MerchantID_Debit AS MerchantID, PPL.ProcessorName FROM PaymentProcessor_Merchant PPM INNER JOIN PaymentProcessor_Lookup PPL ON PPM.PaymentProcessorID = PPL.PaymentProcessorID WHERE TRIM(PPM.MerchantID_Debit)<>'' AND PPM.MerchantID_Debit IS NOT NULL) AS results";
    String UPDATE_MERCHANT = "update PaymentProcessor_Merchant set MerchantID_Credit = ?, MerchantID_Debit = ?, DatedModified = ?, ModifiedBy = ? where PaymentProcessorMerchantID = ?";
    String SAVE_MERCHANT = "INSERT INTO PaymentProcessor_Merchant (LegalEntityAppID,paymentProcessorId,TestOrProd,MerchantID_Credit,MerchantID_Debit,DateCreated,DatedModified,ModifiedBy) values (?, ?, ?, ?, ?, ?, ?, ?)";

    @Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Merchant> findByProcessorId(Long paymentProcessorID){
        LOGGER.debug("MerchantDAO -> findByProcessorId -> paymentProcessorID ={} ", paymentProcessorID);
        List<Merchant> merchantList = jdbcTemplate.query(FIND_ALL_MERCHANT_BY_PROCESSOR_ID, new Object[] { paymentProcessorID }, new MerchantMapper());
        return merchantList;
    }

    public List<String> findAllMerchantsExtCreditDebit(String search){
        LOGGER.debug("MerchantDAO -> findAllMerchantsExtCreditDebit"+ search!=null? "search-> "+search:"");
        List<String> merchantList = jdbcTemplate.queryForList(createQuery(search), createQueryParameters(search), String.class);
        return merchantList;
    }

    private Object[] createQueryParameters(String search){
        Object[] arrayQueryParameters = null;
        if(search != null && !search.isEmpty() &&
                StringUtils.containsIgnoreCase(search, BluefinWebPortalConstants.PROCESSORNAME) &&
                search.split(":").length > 1){
            arrayQueryParameters = search.split(":")[1].replaceAll("\\[|\\]", "").split(",");
        }
        return arrayQueryParameters;
    }

    private String createQuery(String search){
        StringBuilder sb = new StringBuilder(FIND_ALL_MERCHANT_EXT);
        if(search != null && !search.isEmpty() &&
                StringUtils.containsIgnoreCase(search, BluefinWebPortalConstants.PROCESSORNAME) &&
                search.split(":").length > 1){
            sb.append(" WHERE ProcessorName IN ( ");
            String[] arrayProcessorNameParam = search.split(":")[1].split(",");
            for(int i=1; i<=arrayProcessorNameParam.length; i++){
                sb.append(" ?");
                if(i<arrayProcessorNameParam.length){
                    sb.append(" ,");
                }
            }
            sb.append(")");
        }
        sb.append(" ORDER BY MerchantID ASC");
        return sb.toString();
    }

    public boolean save(Merchant merchant){
        boolean saved = Boolean.FALSE;
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        DateTime utc2 = merchant.getDateModified() != null ? merchant.getDateModified().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
        DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
        Timestamp dateUpdated = Timestamp.valueOf(dtf.print(utc2));
        LOGGER.debug("MerchantDAO -> save -> merchant = {} ", merchant.toString());
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SAVE_MERCHANT,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, merchant.getLegalEntityAppID());
                ps.setLong(2, merchant.getPaymentProcessorID());
                ps.setBoolean(3, merchant.isTestOrProd());
                ps.setString(4, merchant.getMerchantIDCredit());
                ps.setString(5, merchant.getMerchantIDDebit());
                ps.setTimestamp(6, dateUpdated);
                ps.setTimestamp(7, dateUpdated);
                ps.setString(8,username);
                return ps;
            });
            saved = true;
        }catch(Exception e){
            LOGGER.error("MerchantDAO -> Error when saving -> merchant = {} ", merchant.toString());
        }
        return saved;
    }

    public boolean update(Merchant merchant){
        boolean saved = Boolean.FALSE;
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        DateTime utc2 = merchant.getDateModified() != null ? merchant.getDateModified().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
        DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
        Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));
        LOGGER.debug("MerchantDAO -> save -> merchant = {} ", merchant.toString());
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(UPDATE_MERCHANT,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, merchant.getMerchantIDCredit());
                ps.setString(2, merchant.getMerchantIDDebit());
                ps.setTimestamp(3, dateModified);
                ps.setString(4,username);
                ps.setLong(5, merchant.getPaymentProcessorMerchantID());
                return ps;
            });
            saved = true;
        }catch(Exception e){
            LOGGER.error("MerchantDAO -> Error when saving -> merchant = {} ", merchant.toString());
        }
        return saved;
    }
}
