package com.mcmcg.ico.bluefin.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.persistent.Transaction;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRemittanceRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.RefundTransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.SaleTransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.VoidTransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.Views;

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
    private PaymentProcessorRemittanceRepository paymentProcessorRemittanceRepository;
    
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
        		PaymentProcessorRemittance paymentProcessorRemittance = paymentProcessorRemittanceRepository.findByProcessorTransactionID(transactionId);
        		Long paymentProcessorId = paymentProcessorRemittance.getPaymentProcessorId();
        		// SaleTransaction uses processorName not paymentProcessorId.
        		// Update processorName with correct value.
            	if (paymentProcessorId != null) {
            		String processorName = getProcessorNameById(paymentProcessorId.toString());
            		paymentProcessorRemittance.setProcessorName(processorName);
            	}
            	// Add Sale transaction which is required for the UI.
            	SaleTransaction saleTransaction = saleTransactionRepository.findByProcessorTransactionId(transactionId);
            	RemittanceSale remittanceSale = new RemittanceSale(paymentProcessorRemittance, saleTransaction);
            	result = remittanceSale;
        	}
            break;
        default:
        	if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
        		result = saleTransactionRepository.findByApplicationTransactionId(transactionId);
        	} else {
        		PaymentProcessorRemittance paymentProcessorRemittance = paymentProcessorRemittanceRepository.findByProcessorTransactionID(transactionId);
        		Long paymentProcessorId = paymentProcessorRemittance.getPaymentProcessorId();
        		// SaleTransaction uses processorName not paymentProcessorId.
        		// Update processorName with correct value.
            	if (paymentProcessorId != null) {
            		String processorName = getProcessorNameById(paymentProcessorId.toString());
            		paymentProcessorRemittance.setProcessorName(processorName);
            	}
            	// Add Sale transaction which is required for the UI.
            	SaleTransaction saleTransaction = saleTransactionRepository.findByProcessorTransactionId(transactionId);
            	RemittanceSale remittanceSale = new RemittanceSale(paymentProcessorRemittance, saleTransaction);
            	result = remittanceSale;
        	}
        }

        if (result == null) {
            throw new CustomNotFoundException("Transaction not found with id = [" + transactionId + "]");
        }

        return result;
    }
    
    /**
     * Get sales and refund transactions. This will be one column of the UI.
     * 
     * @param search
     * @param paging
     * 
     * @return SaleTransaction list
     */
    public Iterable<SaleTransaction> getSalesRefundTransactions(String search, PageRequest paging) {
        Page<SaleTransaction> result;
        try {
        	result = saleTransactionRepository.findSalesRefundTransaction(search, paging);
        } catch (ParseException e) {
            throw new CustomNotFoundException("Unable to process find payment processor remittance, due an error with date formatting");
        }
        final int page = paging.getPageNumber();

        if (page > result.getTotalPages() && page != 0) {
            LOGGER.error("Unable to find the page requested");
            throw new CustomNotFoundException("Unable to find the page requested");
        }

        return result;
    }
    
    /**
     * Get payment process remittance transactions. This will be one column of the UI.
     * 
     * @param search
     * @param paging
     * 
     * @return PaymentProcessorRemittance list
     */
    public Iterable<PaymentProcessorRemittance> getPaymentProcessorRemittances(String search, PageRequest paging) {
        Page<PaymentProcessorRemittance> result;
        try {
        	result = saleTransactionRepository.findRemittanceTransaction(search, paging);
        } catch (ParseException e) {
            throw new CustomNotFoundException("Unable to process find payment processor remittance, due an error with date formatting");
        }
        final int page = paging.getPageNumber();

        if (page > result.getTotalPages() && page != 0) {
            LOGGER.error("Unable to find the page requested");
            throw new CustomNotFoundException("Unable to find the page requested");
        }

        return result;
    }
    
    /**
     * Because the UI needs to display remittance and sale data as pairs, the lists need to be padded empty data.
     * 
     * @param paymentProcessorRemittanceIterable
     * @param saleTransactionIterable
     * @param paging
     * 
     * @return JSON string for the UI.
     */
	public String getAdjustedTransactions(Iterable<PaymentProcessorRemittance> paymentProcessorRemittanceIterable, Iterable<SaleTransaction> saleTransactionIterable, PageRequest paging) {
        
    	String json = "";
    	
        Page<RemittanceSale> result = createAdjustedList(paymentProcessorRemittanceIterable, saleTransactionIterable, paging);
        final int page = paging.getPageNumber();

        if (page > result.getTotalPages() && page != 0) {
            LOGGER.error("Unable to find the page requested");
            throw new CustomNotFoundException("Unable to find the page requested");
        }
		
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JodaModule());
		try {
			json = objectMapper.writerWithView(Views.Summary.class).writeValueAsString(result);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return json;
    }
    
	/**
	 * Because the UI needs to display remittance and sale data as pairs, the lists need to be padded empty data.
	 * 
	 * @param paymentProcessorRemittanceIterable
	 * @param saleTransactionIterable
	 * @param page
	 * 
	 * @return list of pairs (RemittanceSale object)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
    public Page<RemittanceSale> createAdjustedList(Iterable<PaymentProcessorRemittance> paymentProcessorRemittanceIterable, Iterable<SaleTransaction> saleTransactionIterable, PageRequest page) {
    	
    	List<PaymentProcessorRemittance> paymentProcessorRemittanceList = (List) iterableToCollection(paymentProcessorRemittanceIterable);
    	List<SaleTransaction> saleTransactionList = (List) iterableToCollection(saleTransactionIterable);
    	
    	int differenceSize = paymentProcessorRemittanceList.size() - saleTransactionList.size();
    	if (differenceSize > 0) {
    		for (int i=0; i<differenceSize; i++) {
    			SaleTransaction saleTransactionEmpty = new SaleTransaction();
    			saleTransactionList.add(saleTransactionEmpty);
    		}
    	} else if (differenceSize < 0) {
    		for (int i=0; i<(-differenceSize); i++) {
    			PaymentProcessorRemittance paymentProcessorRemittanceEmpty = new PaymentProcessorRemittance();
    			paymentProcessorRemittanceList.add(paymentProcessorRemittanceEmpty);
    		}
    	} else {
    		// Lists are equal in size, do nothing.
    	}
    	
    	// Both lists should be the same size.
    	// Add pairs of remittance and sale object to list.
    	// This is the format required by the UI (list of pairs).
    	List<RemittanceSale> remittanceSaleList = new ArrayList<>();
    	int pageNumber = page.getPageNumber();
        int pageSize = page.getPageSize();
    	int countResult = paymentProcessorRemittanceList.size();
    	for (int i=0; i<paymentProcessorRemittanceList.size(); i++) {
    		PaymentProcessorRemittance paymentProcessorRemittance = paymentProcessorRemittanceList.get(i);
    		SaleTransaction saleTransaction = saleTransactionList.get(i);
    		RemittanceSale remittanceSale = new RemittanceSale(paymentProcessorRemittance, saleTransaction);
    		remittanceSaleList.add(remittanceSale);
    	}
    	Page<RemittanceSale> list = new PageImpl<RemittanceSale>(remittanceSaleList, page, countResult);
    	
    	return list;
    }
    
	/**
	 * Get processorNmae by id
	 * 
	 * @param id
	 * 
	 * @return processorName
	 */
    public String getProcessorNameById(String id) {
    	return saleTransactionRepository.getProcessorNameById(id);
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
    
    /**
     * Convert iterable to collection.
     * 
     * @param iterable
     * 
     * @return collection
     */
    public static <T> Collection<T> iterableToCollection(Iterable<T> iterable) {
    	Collection<T> collection = new ArrayList<>();
    	iterable.forEach(collection::add);
    	return collection;
    }
    
    /**
     * This class will contain a pair of sale and remittance objects.
     * It's a requirement of the UI.
     *
     */
    public class RemittanceSale implements Transaction {
    	
    	private PaymentProcessorRemittance paymentProcessorRemittance;
    	private SaleTransaction saleTransaction;
    	
    	public RemittanceSale() {
    	}
    	
    	public RemittanceSale(PaymentProcessorRemittance paymentProcessorRemittance, SaleTransaction saleTransaction) {
    		this.paymentProcessorRemittance = paymentProcessorRemittance;
    		this.saleTransaction = saleTransaction;
    	}
    	
    	public PaymentProcessorRemittance getPaymentProcessorRemittance() {
    		return paymentProcessorRemittance;
    	}
    	
    	public SaleTransaction getSaleTransaction() {
    		return saleTransaction;
    	}

    	@Override
    	public String getApplicationTransactionId() {
    		return null;
    	}

    	@Override
    	public String getProcessorTransactionId() {
    		return null;
    	}

    	@Override
    	public String getMerchantId() {
    		return null;
    	}

    	@Override
    	public String getTransactionType() {
    		return null;
    	}

    	@Override
    	public DateTime getTransactionDateTime() {
    		return null;
    	}
    }
}
