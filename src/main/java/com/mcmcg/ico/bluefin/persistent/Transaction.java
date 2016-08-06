package com.mcmcg.ico.bluefin.persistent;

import java.util.Date;

public interface Transaction {
    public String getApplicationTransactionId();

    public String getProcessorTransactionId();

    public String getMerchantId();

    public String getTransactionType();

    public Date getTransactionDateTime();
}
