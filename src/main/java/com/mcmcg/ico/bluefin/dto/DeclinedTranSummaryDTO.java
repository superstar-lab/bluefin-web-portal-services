package com.mcmcg.ico.bluefin.dto;

import lombok.Data;

@Data
public class DeclinedTranSummaryDTO {
    public String processor;
    public String declined;
    public String approved;
    public String rate;
}
