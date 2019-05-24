package com.mcmcg.ico.bluefin.model;

import java.util.List;

import lombok.Data;

@Data
public class BatchReturnFileModel {

	List<SaleTransaction> result;
	BatchUpload batchUpload;
}
