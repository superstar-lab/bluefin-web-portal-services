package com.mcmcg.ico.bluefin.batch.file;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.factory.BatchReturnFile;
import com.mcmcg.ico.bluefin.model.BatchFileObjects;
import com.mcmcg.ico.bluefin.model.BatchReturnFileModel;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.StatusCode;

@Component
public class MCMBatchReturnFile extends BatchReturnFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(MCMBatchReturnFile.class);
	
	@Override
	public void generateBatchReturnFile(BatchReturnFileModel batchReturnFileModel, BatchFileObjects batchFileObjects, 
			SaleTransaction saleTransaction, List<String> saleTransactionDataRecord) throws IOException {

		LOGGER.info("adding values for MCM batch returned file");
		// Invoice
		saleTransactionDataRecord.add(saleTransaction.getInvoiceNumber());

		BigDecimal amount = saleTransaction.getChargeAmount().setScale(2, BigDecimal.ROUND_DOWN);
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(0);
		df.setGroupingUsed(false);

		// Amount
		saleTransactionDataRecord.add(saleTransaction.getChargeAmount() == null ? "" : df.format(amount));

		// Result
		saleTransactionDataRecord.add(StatusCode.getStatusCode(saleTransaction.getInternalStatusCode()));

		// Error Message
		String errorMessage = saleTransaction.getInternalResponseDescription() + "("
				+ saleTransaction.getInternalResponseCode() + ")";
		saleTransactionDataRecord.add(errorMessage.replaceAll("\r", "").replaceAll("\n", ""));
		
		LOGGER.info("Values for MCM batch returned file added successfully");
	}

	@Override
	public Object[] createFileHeader() {
		return BluefinWebPortalConstants.TRANSACTIONS_MCM_FILE_HEADER;	
	}

}
