package com.mcmcg.ico.bluefin.dto;

import lombok.Data;

@Data
public class TopTranSummaryDTO {
    public String processor;
    public String declineReason;
    public String totalTransactions;
    public String rate;
}
