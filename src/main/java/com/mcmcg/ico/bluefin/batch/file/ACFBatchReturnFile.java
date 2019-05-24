package com.mcmcg.ico.bluefin.batch.file;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.factory.BatchReturnFile;
import com.mcmcg.ico.bluefin.model.BatchFileObjects;
import com.mcmcg.ico.bluefin.model.BatchReturnFileModel;
import com.mcmcg.ico.bluefin.model.SaleTransaction;

@Component
public class ACFBatchReturnFile extends BatchReturnFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(ACFBatchReturnFile.class);
	
	@Override
	public void generateBatchReturnFile(BatchReturnFileModel batchReturnFileModel, BatchFileObjects batchFileObjects, 
			SaleTransaction saleTransaction, List<String> saleTransactionDataRecord) throws IOException {

		LOGGER.info("adding values for ACF batch returned file");
		// Invoice
		saleTransactionDataRecord.add(saleTransaction.getInvoiceNumber());

		BigDecimal amount = saleTransaction.getChargeAmount().setScale(2, BigDecimal.ROUND_DOWN);
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(0);
		df.setGroupingUsed(false);
				
		//Customer Name
		saleTransactionDataRecord.add(saleTransaction.getFirstName()+" "+saleTransaction.getLastName());
				
		//Card Type
		saleTransactionDataRecord.add(saleTransaction.getCardType());
				
		//Card Number
		String cardMiddleDigits = saleTransaction.getToken().substring(6, saleTransaction.getToken().length()-4);
		String starsForCardMiddleDigits = StringUtils.repeat("X", cardMiddleDigits.length());
		saleTransactionDataRecord.add(saleTransaction.getCardNumberFirst6Char()+starsForCardMiddleDigits+saleTransaction.getCardNumberLast4Char().replaceAll("XXXX-", ""));
				
		//Amount
		saleTransactionDataRecord.add(saleTransaction.getChargeAmount() == null ? "" : df.format(amount));

		//Source
		saleTransactionDataRecord.add("uploaded");
				
		if("1".equalsIgnoreCase(saleTransaction.getInternalStatusCode())) {
			//Auth
			saleTransactionDataRecord.add(saleTransaction.getInternalResponseCode());
		} else {
			//Error Message
			String errorMessage = saleTransaction.getInternalResponseDescription() + "("
					+ saleTransaction.getInternalResponseCode() + ")";
			saleTransactionDataRecord.add(errorMessage.replaceAll("\r", "").replaceAll("\n", ""));
		}
				
		//AVS
		saleTransactionDataRecord.add(" ");
				
		//CVV2
		saleTransactionDataRecord.add(" ");
				
		//Error Code
		saleTransactionDataRecord.add("0");
		
		LOGGER.info("Values for ACF batch returned file added successfully");
	}

	@Override
	public Object[] createFileHeader() {
		return BluefinWebPortalConstants.TRANSACTIONS_ACF_FILE_HEADER;	
	}
}
