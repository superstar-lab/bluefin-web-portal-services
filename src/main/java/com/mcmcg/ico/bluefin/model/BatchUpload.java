package com.mcmcg.ico.bluefin.model;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BatchUpload {
    private Long batchUploadId;
    private String batchApplication;
    private String name;
    private String fileName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy hh:mm:ss a")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private DateTime dateUploaded;
    @JsonProperty("UpLoadedBy")
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
    @JsonIgnore
    private Long legalEntityAppId;
    private String legalEntityName;
	public Long getBatchUploadId() {
		return batchUploadId;
	}
	public void setBatchUploadId(Long batchUploadId) {
		this.batchUploadId = batchUploadId;
	}
	public String getBatchApplication() {
		return batchApplication;
	}
	public void setBatchApplication(String batchApplication) {
		this.batchApplication = batchApplication;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public DateTime getDateUploaded() {
		return dateUploaded;
	}
	public void setDateUploaded(DateTime dateUploaded) {
		this.dateUploaded = dateUploaded;
	}
	public String getUpLoadedBy() {
		return upLoadedBy;
	}
	public void setUpLoadedBy(String upLoadedBy) {
		this.upLoadedBy = upLoadedBy;
	}
	public DateTime getProcessStart() {
		return processStart;
	}
	public void setProcessStart(DateTime processStart) {
		this.processStart = processStart;
	}
	public DateTime getProcessEnd() {
		return processEnd;
	}
	public void setProcessEnd(DateTime processEnd) {
		this.processEnd = processEnd;
	}
	public int getNumberOfTransactions() {
		return numberOfTransactions;
	}
	public void setNumberOfTransactions(int numberOfTransactions) {
		this.numberOfTransactions = numberOfTransactions;
	}
	public int getNumberOfTransactionsProcessed() {
		return numberOfTransactionsProcessed;
	}
	public void setNumberOfTransactionsProcessed(int numberOfTransactionsProcessed) {
		this.numberOfTransactionsProcessed = numberOfTransactionsProcessed;
	}
	public int getNumberOfApprovedTransactions() {
		return numberOfApprovedTransactions;
	}
	public void setNumberOfApprovedTransactions(int numberOfApprovedTransactions) {
		this.numberOfApprovedTransactions = numberOfApprovedTransactions;
	}
	public int getNumberOfDeclinedTransactions() {
		return numberOfDeclinedTransactions;
	}
	public void setNumberOfDeclinedTransactions(int numberOfDeclinedTransactions) {
		this.numberOfDeclinedTransactions = numberOfDeclinedTransactions;
	}
	public int getNumberOfErrorTransactions() {
		return numberOfErrorTransactions;
	}
	public void setNumberOfErrorTransactions(int numberOfErrorTransactions) {
		this.numberOfErrorTransactions = numberOfErrorTransactions;
	}
	public int getNumberOfRejected() {
		return numberOfRejected;
	}
	public void setNumberOfRejected(int numberOfRejected) {
		this.numberOfRejected = numberOfRejected;
	}
	public Long getLegalEntityAppId() {
		return legalEntityAppId;
	}
	public void setLegalEntityAppId(Long legalEntityAppId) {
		this.legalEntityAppId = legalEntityAppId;
	}
	public String getLegalEntityName() {
		return legalEntityName;
	}
	public void setLegalEntityName(String legalEntityName) {
		this.legalEntityName = legalEntityName;
	}
    
    
}
