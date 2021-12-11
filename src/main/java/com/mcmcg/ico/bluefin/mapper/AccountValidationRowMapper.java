package com.mcmcg.ico.bluefin.mapper;

import com.mcmcg.ico.bluefin.model.AccountValidation;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountValidationRowMapper implements RowMapper<AccountValidation> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountValidationRowMapper.class);
    @Override
    public AccountValidation mapRow(ResultSet rs, int row) throws SQLException {
        AccountValidation accountValidation = new AccountValidation();

        accountValidation.setAccountValidationID( rs.getLong( "accountValidationID" ));
        accountValidation.setApplicationRequestID( rs.getString( "applicationRequestID" ));
        accountValidation.setValidationType( rs.getString( "validationType" ));
        accountValidation.setApplication( rs.getString( "application" ));
        accountValidation.setMcmAccountID( rs.getString( "mcmAccountID" ));
        accountValidation.setCompleteAppRequest( rs.getString( "completeAppRequest" ));
        accountValidation.setCompleteBofaRequest( rs.getString( "completeBofaRequest" ));
        try {
            accountValidation.setRequestDateTime( new DateTime(rs.getTimestamp("requestDateTime" ) ) );
        } catch (SQLException sqlEcp){
            LOGGER.debug("Invalid value found for expiry date");
        }
        try {
            if (rs.getTimestamp("responseDateTime") == null) {
                accountValidation.setResponseDateTime(null);
            } else {
                accountValidation.setResponseDateTime(new DateTime(rs.getTimestamp("responseDateTime")));
            }
        } catch (SQLException sqlEcp){
            LOGGER.debug("Invalid value found for expiry date");
        }
        accountValidation.setRequestResponseDescription( rs.getString( "requestResponseDescription" ));
        accountValidation.setAccountStatusCode( rs.getString( "accountStatusCode" ));
        accountValidation.setAccountStatusDescription( rs.getString( "accountStatusDescription" ));
        accountValidation.setCompleteResponse( rs.getString( "completeResponse" ));
        accountValidation.setRequestResponseCode( rs.getString( "requestResponseCode" ));
        return accountValidation;
    }
}