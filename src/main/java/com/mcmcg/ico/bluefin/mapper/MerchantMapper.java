package com.mcmcg.ico.bluefin.mapper;

import com.mcmcg.ico.bluefin.model.Merchant;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MerchantMapper implements RowMapper<Merchant> {
    @Override
    public Merchant mapRow(ResultSet rs, int rowNum) throws SQLException {
        Merchant merchant = new Merchant();
        merchant.setPaymentProcessorMerchantID(rs.getLong("PaymentProcessorMerchantID"));
        merchant.setLegalEntityAppID(rs.getLong("LegalEntityAppID"));
        merchant.setPaymentProcessorID(rs.getLong("PaymentProcessorID"));
        merchant.setTestOrProd(rs.getBoolean("TestOrProd"));
        merchant.setMerchantIDCredit(rs.getString("MerchantID_Credit"));
        merchant.setMerchantIDDebit(rs.getString("MerchantID_Debit"));
        Timestamp ts;
        if(rs.getString("DateCreated") != null) {
            ts = Timestamp.valueOf(rs.getString("DateCreated"));
            merchant.setDateCreated(new DateTime(ts));
        }
        if(rs.getString("DatedModified") != null) {
            ts = Timestamp.valueOf(rs.getString("DatedModified"));
            merchant.setDateModified(new DateTime(ts));
        }
        merchant.setModifiedBy(rs.getString("ModifiedBy"));
        return merchant;
    }
}
