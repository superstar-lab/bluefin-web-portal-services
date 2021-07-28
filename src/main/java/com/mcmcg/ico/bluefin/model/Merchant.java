package com.mcmcg.ico.bluefin.model;

import lombok.Data;

@Data
public class Merchant  extends Common{

    private Long paymentProcessorMerchantID;
    private Long legalEntityAppID;
    private Long paymentProcessorID;
    private boolean testOrProd;
    private String merchantIDCredit;
    private String merchantIDDebit;

}
