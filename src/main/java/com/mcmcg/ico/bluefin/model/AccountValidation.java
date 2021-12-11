package com.mcmcg.ico.bluefin.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class AccountValidation {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    public Long accountValidationID;
    public String applicationRequestID;
    public String validationType;
    public String application;
    public String mcmAccountID;
    public String completeAppRequest;
    public String completeBofaRequest;
    public String requestResponseCode;
    public String requestResponseDescription;
    public DateTime requestDateTime = new DateTime();
    public String accountStatusCode;
    public String accountStatusDescription;
    public String completeResponse;
    public DateTime responseDateTime = new DateTime();
}
