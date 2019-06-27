package com.mcmcg.ico.bluefin.model;

public class BatchFileRequest {

	private long batchId;
	private String base64File;
	private String legalEntityName;
	
	public BatchFileRequest(long batchId, String file, String legalEntityName) {
		this.batchId = batchId;
		this.base64File = file;
		this.legalEntityName = legalEntityName;
	}

	public long getBatchId() {
		return batchId;
	}

	public void setBatchId(long batchId) {
		this.batchId = batchId;
	}

	public String getBase64File() {
		return base64File;
	}

	public void setBase64File(String base64File) {
		this.base64File = base64File;
	}

	public String getLegalEntityName() {
		return legalEntityName;
	}

	public void setLegalEntityName(String legalEntityName) {
		this.legalEntityName = legalEntityName;
	}
	
	
}