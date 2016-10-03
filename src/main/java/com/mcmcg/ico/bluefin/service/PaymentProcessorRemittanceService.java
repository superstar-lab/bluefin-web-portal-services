package com.mcmcg.ico.bluefin.service;

import java.text.ParseException;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.persistent.Transaction;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.ReconciliationStatusRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.RefundTransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.SaleTransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.VoidTransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

@Service
@Transactional
public class PaymentProcessorRemittanceService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRemittanceService.class);
	
    @Autowired
    private SaleTransactionRepository saleTransactionRepository;
    @Autowired
    private VoidTransactionRepository voidTransactionRepository;
    @Autowired
    private RefundTransactionRepository refundTransactionRepository;
    @Autowired
    private PaymentProcessorRepository paymentProcessorRepository;
    @Autowired
    private ReconciliationStatusRepository reconciliationStatusRepository;
    
    /**
     * Get transaction information for details page.
     * 
     * @param transactionId
     * @param transactionType
     * @param processorTransactionType
     * 
     * @return transaction details
     */
    public Transaction getTransactionInformation(final String transactionId, TransactionType transactionType, final String processorTransactionType) {
        Transaction result = null;

        switch (transactionType) {
        case VOID:
        	if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
        		result = voidTransactionRepository.findByApplicationTransactionId(transactionId);
        	} else {
        		result = voidTransactionRepository.findByProcessorTransactionId(transactionId);
        	}
            break;
        case REFUND:
        	if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
        		result = refundTransactionRepository.findByApplicationTransactionId(transactionId);
        	} else {
        		result = refundTransactionRepository.findByProcessorTransactionId(transactionId);
        	}
            break;
        case REMITTANCE:
        	// PaymentProcessor_Remittance does not have ApplicationTransactionId
        	if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
        		result = null;
        	} else {
        		try {
        			result = saleTransactionRepository.getRemittanceSaleRefundVoidByProcessorTransactionId("processorTransactionId:" + transactionId, QueryDSLUtil.getPageRequest(0, 1, ""));
        		} catch (ParseException e) {
        			e.printStackTrace();
        		}
        	}
        	break;
        case SALE:
        case TOKENIZE:	
        default:
        	if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
        		result = saleTransactionRepository.findByApplicationTransactionId(transactionId);
        	} else {
        		try {
        			result = saleTransactionRepository.getRemittanceSaleRefundVoidByProcessorTransactionId("processorTransactionId:" + transactionId, QueryDSLUtil.getPageRequest(0, 1, ""));
        		} catch (ParseException e) {
        			e.printStackTrace();
        		}
        	}
        }

        if (result == null) {
            throw new CustomNotFoundException("Transaction not found with id = [" + transactionId + "]");
        }

        return result;
    }
    
    /**
     * Get remittance, sale, refund, and void transactions. This will be one column of the UI.
     * 
     * @param search
     * @param paging
     * @param negate
     * 
     * @return list of objects containing these transactions
     */
    public Iterable<PaymentProcessorRemittance> getRemittanceSaleRefundVoidTransactions(String search, PageRequest paging, boolean negate) {
        Page<PaymentProcessorRemittance> result;
        try {
        	result = saleTransactionRepository.findRemittanceSaleRefundVoidTransactions(search, paging, negate);
        } catch (ParseException e) {
            throw new CustomNotFoundException("Unable to process find remittance, sale, refund or void transactions, due an error with date formatting");
        }
        final int page = paging.getPageNumber();

        if (page > result.getTotalPages() && page != 0) {
            LOGGER.error("Unable to find the page requested");
            throw new CustomNotFoundException("Unable to find the page requested");
        }

        return result;
    }
    
	/**
	 * Get processorName by id
	 * 
	 * @param id
	 * 
	 * @return processorName
	 */
    public String getProcessorNameById(String paymentProcessorId) {
    	return paymentProcessorRepository.findByPaymentProcessorId(Long.parseLong(paymentProcessorId)).getProcessorName();
    }
    
    /**
     * Get reconciliationStatusId by reconciliationStatus
     * 
     * @param reconciliationStatus
     * 
     * @return reconciliationStatusId
     */
    public String getReconciliationStatusId(String reconciliationStatus) {
    	return reconciliationStatusRepository.findByReconciliationStatus(reconciliationStatus).getReconciliationStatusId().toString();
    }
    
    /**
     * Get the value of parameter in search string.
     * 
     * @param search string
     * @param parameter in search string
     * 
     * @return value of parameter
     */
    public String getValueFromParameter(String search, String parameter) {
    	
    	String value = null;
    	String[] array1 = search.split("\\$\\$");
    	
    	for (String pair : array1) {
    		if (pair.startsWith(parameter)) {
    			String[] array2 = pair.split(":");
    			value = array2[1];
    			break;
    		}
    	}
    	
        return value;
    }
}
