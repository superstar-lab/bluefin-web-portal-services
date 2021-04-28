package com.mcmcg.ico.bluefin.mapper;

import com.mcmcg.ico.bluefin.model.SaleTransactionInfo;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionRowMapper implements RowMapper<SaleTransactionInfo>{
    @Override
    public SaleTransactionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
        SaleTransactionInfo objSaleTran = new SaleTransactionInfo();
        objSaleTran.setId(rs.getString("ApplicationTransactionID"));
        objSaleTran.setExpDate(rs.getString("ExpiryDate"));
        objSaleTran.setToken(rs.getString("Token"));
        objSaleTran.setChargeAmount(rs.getString("ChargeAmount"));
        objSaleTran.setAccountNo(rs.getString("AccountId"));
        objSaleTran.setStatus(rs.getString("PaymentProcessorStatusCodeDescription"));
        objSaleTran.setTransactionDateTime(rs.getString("TransactionDateTime"));
        objSaleTran.setApplication(rs.getString("LegalEntityApp"));
        objSaleTran.setInternalStatusCode(rs.getString("InternalStatusCode"));
        return objSaleTran;
    }
}

