package com.mcmcg.ico.bluefin.batch.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
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
import com.mcmcg.ico.bluefin.model.StatusCode;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;

@Component
public class MCMBatchReturnFile extends BatchReturnFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(MCMBatchReturnFile.class);
	
	private static final Object[] TRANSACTIONS_MCM_FILE_HEADER = { "Date", "Time", "Invoice", "Amount", "Result",
	"Error Message" };
	
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
		return TRANSACTIONS_MCM_FILE_HEADER;	
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
