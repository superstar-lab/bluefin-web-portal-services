package com.mcmcg.ico.bluefin.model;

import lombok.Data;

@Data
public class AccountValidationRequest {

    private String startDate;
    private String endDate;
    private Integer page;
    private Integer size;
    private String timeZone;
    private String sort;

}
