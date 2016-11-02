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
import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.persistent.RefundTransaction;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.persistent.Transaction;
import com.mcmcg.ico.bluefin.persistent.VoidTransaction;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRemittanceRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.ReconciliationStatusRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.RefundTransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.SaleTransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.VoidTransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

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
    public Transaction getTransactionInformation(final String transactionId, TransactionType transactionType,
            final String processorTransactionType) {
        Transaction result = null;

        switch (transactionType) {
        case VOID:
            if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
                result = voidTransactionRepository.findByApplicationTransactionId(transactionId);
            } else {
                PaymentProcessorRemittance ppr = paymentProcessorRemittanceRepository
                        .findByProcessorTransactionId(transactionId);
                VoidTransaction vt = voidTransactionRepository.findByProcessorTransactionId(transactionId);
                String processorName = paymentProcessorRepository.findByPaymentProcessorId(ppr.getPaymentProcessorId())
                        .getProcessorName();

                PaymentProcessorRemittance paymentProcessorRemittance = new PaymentProcessorRemittance(
                        ppr.getPaymentProcessorRemittanceId(), ppr.getCreatedDate(), ppr.getReconciliationStatusId(),
                        ppr.getReconciliationDate(), ppr.getPaymentMethod(), ppr.getTransactionAmount(),
                        ppr.getTransactionType(), ppr.getTransactionTime(), ppr.getAccountId(), ppr.getApplication(),
                        ppr.getProcessorTransactionId(), ppr.getMerchantId(), ppr.getTransactionSource(),
                        ppr.getFirstName(), ppr.getLastName(), ppr.getRemittanceCreationDate(),
                        ppr.getPaymentProcessorId(), processorName, vt.getVoidTransactionId(), vt.getTransactionType(),
                        null, null, vt.getApplicationTransactionId(), vt.getProcessorTransactionId(),
                        vt.getMerchantId(), vt.getTransactionDateTime(), null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, vt.getPaymentProcessorResponseCode(),
                        vt.getPaymentProcessorResponseCodeDescription(), vt.getApprovalCode(),
                        vt.getInternalResponseCode(), vt.getInternalResponseDescription(), vt.getInternalStatusCode(),
                        vt.getInternalStatusDescription(), vt.getPaymentProcessorStatusCode(),
                        vt.getPaymentProcessorStatusCodeDescription(), null, null, null, null, null, null,
                        vt.getProcessUser(), vt.getProcessorName(), vt.getApplication(), null, null, null, null, null,
                        null, null, vt.getCreatedDate(), 1, 0, null, null, null, null, null);

                result = paymentProcessorRemittance;
            }
            break;
        case REFUND:
            if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
                result = refundTransactionRepository.findByApplicationTransactionId(transactionId);
            } else {
                PaymentProcessorRemittance ppr = paymentProcessorRemittanceRepository
                        .findByProcessorTransactionId(transactionId);
                RefundTransaction rt = refundTransactionRepository.findByProcessorTransactionId(transactionId);
                String processorName = paymentProcessorRepository.findByPaymentProcessorId(ppr.getPaymentProcessorId())
                        .getProcessorName();

                PaymentProcessorRemittance paymentProcessorRemittance = new PaymentProcessorRemittance(
                        ppr.getPaymentProcessorRemittanceId(), ppr.getCreatedDate(), ppr.getReconciliationStatusId(),
                        ppr.getReconciliationDate(), ppr.getPaymentMethod(), ppr.getTransactionAmount(),
                        ppr.getTransactionType(), ppr.getTransactionTime(), ppr.getAccountId(), ppr.getApplication(),
                        ppr.getProcessorTransactionId(), ppr.getMerchantId(), ppr.getTransactionSource(),
                        ppr.getFirstName(), ppr.getLastName(), ppr.getRemittanceCreationDate(),
                        ppr.getPaymentProcessorId(), processorName, rt.getRefundTransactionId(),
                        rt.getTransactionType(), null, null, rt.getApplicationTransactionId(),
                        rt.getProcessorTransactionId(), rt.getMerchantId(), rt.getTransactionDateTime(), null, null,
                        null, rt.getRefundAmount(), null, null, null, null, null, null, null, null, null, null, null,
                        null, rt.getPaymentProcessorResponseCode(), rt.getPaymentProcessorResponseCodeDescription(),
                        rt.getApprovalCode(), rt.getInternalResponseCode(), rt.getInternalResponseDescription(),
                        rt.getInternalStatusCode(), rt.getInternalStatusDescription(),
                        rt.getPaymentProcessorStatusCode(), rt.getPaymentProcessorStatusCodeDescription(), null, null,
                        null, null, null, null, rt.getProcessUser(), rt.getProcessorName(), rt.getApplication(), null,
                        null, null, null, null, null, null, rt.getCreatedDate(), 0, 1, null, null,
                        rt.getReconciliationStatusID(), rt.getReconciliationDate(), null);

                result = paymentProcessorRemittance;
            }
            break;
        case REMITTANCE:
            // PaymentProcessor_Remittance does not have
            // ApplicationTransactionId
            if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
                result = null;
            } else {
                result = getRemittanceSaleResult(transactionId);
            }
            break;
        case SALE:
        case TOKENIZE:
        default:
            if (processorTransactionType.equalsIgnoreCase("BlueFin")) {
                result = saleTransactionRepository.findByApplicationTransactionId(transactionId);
            } else {
                result = getRemittanceSaleResult(transactionId);
            }
        }

        if (result == null) {
            throw new CustomNotFoundException("Transaction not found with id = [" + transactionId + "]");
        }

        return result;
    }

    public Transaction getRemittanceSaleResult(String transactionId) {
        Transaction result = null;

        PaymentProcessorRemittance ppr = paymentProcessorRemittanceRepository
                .findByProcessorTransactionId(transactionId);
        if (ppr == null) {
            ppr = new PaymentProcessorRemittance();
        }
        SaleTransaction st = saleTransactionRepository.findByProcessorTransactionId(transactionId);
        if (st == null) {
            st = new SaleTransaction();
        }
        String processorName = null;
        if (ppr != null) {
            Long paymentProcessorId = ppr.getPaymentProcessorId();
            if (paymentProcessorId != null) {
                PaymentProcessor paymentProcessor = paymentProcessorRepository
                        .findByPaymentProcessorId(paymentProcessorId);
                processorName = paymentProcessor.getProcessorName();
            }
        }
        Short tokenized = null;
        if (st != null) {
            String tokenizedStr = st.getTokenized();
            if (tokenizedStr != null) {
                if (tokenizedStr.equalsIgnoreCase("No")) {
                    tokenized = 0;
                } else {
                    tokenized = 1;
                }
            }
        }

        PaymentProcessorRemittance paymentProcessorRemittance = new PaymentProcessorRemittance(
                ppr.getPaymentProcessorRemittanceId(), ppr.getCreatedDate(), ppr.getReconciliationStatusId(),
                ppr.getReconciliationDate(), ppr.getPaymentMethod(), ppr.getTransactionAmount(),
                ppr.getTransactionType(), ppr.getTransactionTime(), ppr.getAccountId(), ppr.getApplication(),
                ppr.getProcessorTransactionId(), ppr.getMerchantId(), ppr.getTransactionSource(), ppr.getFirstName(),
                ppr.getLastName(), ppr.getRemittanceCreationDate(), ppr.getPaymentProcessorId(), processorName,
                st.getSaleTransactionId(), st.getTransactionType(), st.getLegalEntity(), st.getAccountNumber(),
                st.getApplicationTransactionId(), st.getProcessorTransactionId(), st.getMerchantId(),
                st.getTransactionDateTime(), st.getCardNumberFirst6Char(), st.getCardNumberLast4Char(),
                st.getCardType(), st.getAmount(), st.getExpiryDate(), st.getFirstName(), st.getLastName(),
                st.getAddress1(), st.getAddress2(), st.getCity(), st.getState(), st.getPostalCode(), st.getCountry(),
                st.getTestMode(), st.getToken(), tokenized, st.getProcessorResponseCode(),
                st.getProcessorResponseCodeDescription(), st.getApprovalCode(), st.getInternalResponseCode(),
                st.getInternalResponseDescription(), st.getInternalStatusCode(), st.getInternalStatusDescription(),
                st.getPaymentProcessorStatusCode(), st.getPaymentProcessorStatusCodeDescription(),
                st.getPaymentProcessorRuleId(), st.getRulePaymentProcessorId(), st.getRuleCardType(),
                st.getRuleMaximumMonthlyAmount(), st.getRuleNoMaximumMonthlyAmountFlag(), st.getRulePriority(),
                st.getProcessUser(), st.getProcessorName(), st.getApplication(), st.getOrigin(), st.getAccountPeriod(),
                st.getDesk(), st.getInvoiceNumber(), st.getUserDefinedField1(), st.getUserDefinedField2(),
                st.getUserDefinedField3(), st.getCreatedDate(), st.getIsVoided(), st.getIsRefunded(),
                st.getPaymentProcessorInternalStatusCodeId(), st.getPaymentProcessorInternalResponseCodeId(),
                st.getReconciliationStatusId(), st.getReconciliationDate(), st.getBatchUploadId());

        result = paymentProcessorRemittance;

        return result;
    }

    /**
     * Get remittance, sale, refund, and void transactions. This will be one
     * column of the UI.
     * 
     * @param search
     * @param paging
     * @param negate
     * 
     * @return list of objects containing these transactions
     */
    public Iterable<PaymentProcessorRemittance> getRemittanceSaleRefundVoidTransactions(String search,
            PageRequest paging, boolean negate) {
        Page<PaymentProcessorRemittance> result;
        try {
            result = saleTransactionRepository.findRemittanceSaleRefundTransactions(search, paging, negate);
        } catch (ParseException e) {
            throw new CustomNotFoundException(
                    "Unable to process find remittance, sale, refund or void transactions, due an error with date formatting");
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
        return paymentProcessorRepository.findByPaymentProcessorId(Long.parseLong(paymentProcessorId))
                .getProcessorName();
    }

    /**
     * Get reconciliationStatusId by reconciliationStatus
     * 
     * @param reconciliationStatus
     * 
     * @return reconciliationStatusId
     */
    public String getReconciliationStatusId(String reconciliationStatus) {
        return reconciliationStatusRepository.findByReconciliationStatus(reconciliationStatus)
                .getReconciliationStatusId().toString();
    }

    /**
     * Get the value of parameter in search string.
     * 
     * @param search
     *            string
     * @param parameter
     *            in search string
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
