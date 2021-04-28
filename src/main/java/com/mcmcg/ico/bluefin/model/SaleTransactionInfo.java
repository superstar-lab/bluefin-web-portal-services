package com.mcmcg.ico.bluefin.model;

import lombok.Data;

@Data
public class SaleTransactionInfo {
    private String id;
    private String accountNo;
    private String chargeAmount;
    private String expDate;
    private String status;
    private String token;
    private String transactionDateTime;
    private String application;
    private String updateReason;
    private String internalStatusCode;
}
