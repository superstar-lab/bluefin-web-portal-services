package com.mcmcg.ico.bluefin.model;

import java.util.List;

public class BatchReturnFileModel {

	List<SaleTransaction> result;
	BatchUpload batchUpload;
	public List<SaleTransaction> getResult() {
		return result;
	}
	public void setResult(List<SaleTransaction> result) {
		this.result = result;
	}
	public BatchUpload getBatchUpload() {
		return batchUpload;
	}
	public void setBatchUpload(BatchUpload batchUpload) {
		this.batchUpload = batchUpload;
	}
	
	
}
