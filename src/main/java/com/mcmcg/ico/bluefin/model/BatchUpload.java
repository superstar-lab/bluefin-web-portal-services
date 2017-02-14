package com.mcmcg.ico.bluefin.model;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class BatchUpload {
    private Long batchUploadId;
    private String batchApplication;
    private String name;
    private String fileName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy hh:mm:ss a")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private DateTime dateUploaded;
    private String upLoadedBy;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy hh:mm:ss a")
    private DateTime processStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy hh:mm:ss a")
    private DateTime processEnd;
    private int numberOfTransactions;
    private int numberOfTransactionsProcessed;
    private int numberOfApprovedTransactions;
    private int numberOfDeclinedTransactions;
    private int numberOfErrorTransactions;
    private int numberOfRejected;
}
