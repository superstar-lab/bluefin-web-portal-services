package com.mcmcg.ico.bluefin.batch.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.factory.BatchReturnFile;
import com.mcmcg.ico.bluefin.model.BatchFileObjects;
import com.mcmcg.ico.bluefin.model.BatchReturnFileModel;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;

@Component
public class ACFBatchReturnFile extends BatchReturnFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(ACFBatchReturnFile.class);
	
	private static final Object[] TRANSACTIONS_ACF_FILE_HEADER = { "Date","Time","Invoice","Customer","Card Type","Card Number","Amount","Source",
			"Auth","AVS","CVV2","Error Code"};
	
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
		return TRANSACTIONS_ACF_FILE_HEADER;	
	}
	
	@Override
	public ResponseEntity<String> deleteTempFile(File downloadFile, HttpServletResponse response, String deleteTempFile) {
		InputStream inputStream = null;
		try {
			inputStream = FileUtils.openInputStream(downloadFile);
			response.setContentType(BluefinWebPortalConstants.APPOCTSTREAM);
			response.setHeader(BluefinWebPortalConstants.CONTENTDISPOSITION, BluefinWebPortalConstants.ATTACHMENTFILENAME + downloadFile.getName());
			
			FileCopyUtils.copy(inputStream, response.getOutputStream());
			LOGGER.debug(deleteTempFile, downloadFile.getName());
			boolean deleted = downloadFile.delete();
			LOGGER.debug("File deleted ? {}",deleted);
			return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
		} catch(Exception e) {
			LOGGER.error("An error occured to during getRemittanceTransactionsReport file= "+e);
			throw new CustomException("An error occured to during getRemittanceTransactionsReport file.");
		}
		finally {
		    	if(inputStream!=null) {
		    		try {
		    			inputStream.close();
					} catch (IOException e) {
						LOGGER.error("An error occured to close input stream= "+e);
					}
		    	}
		}
		
	}
	
}
