package com.mcmcg.ico.bluefin.persistent;

import org.joda.time.DateTime;

public interface Transaction {
    public String getApplicationTransactionId();

    public String getProcessorTransactionId();

    public String getMerchantId();

    public String getTransactionType();

    public DateTime getTransactionDateTime();
}
