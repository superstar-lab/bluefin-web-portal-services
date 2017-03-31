package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.mcmcg.ico.bluefin.rest.resource.Views;

public class PaymentProcessorRemittance implements Serializable, Transaction {

	

	private static final long serialVersionUID = -7696931237337114459L;

	public PaymentProcessorRemittance() {
	}

	public PaymentProcessorRemittance(Long paymentProcessorRemittanceId, DateTime dateCreated,
			Long reconciliationStatusId, DateTime reconciliationDate, String paymentMethod,
			BigDecimal transactionAmount, String transactionType, DateTime transactionTime, String accountId,
			String application, String processorTransactionId, String merchantId, String transactionSource,
			String firstName, String lastName, DateTime remittanceCreationDate, Long paymentProcessorId,
			String reProcessStatus, Long etlRunId) {
		this.paymentProcessorRemittanceId = paymentProcessorRemittanceId;
		this.dateCreated = dateCreated;
		this.reconciliationStatusId = reconciliationStatusId;
		this.reconciliationDate = reconciliationDate;
		this.paymentMethod = paymentMethod;
		this.transactionAmount = transactionAmount;
		this.transactionType = transactionType;
		this.transactionTime = transactionTime;
		this.accountId = accountId;
		this.application = application;
		this.processorTransactionId = processorTransactionId;
		this.merchantId = merchantId;
		this.transactionSource = transactionSource;
		this.firstName = firstName;
		this.lastName = lastName;
		this.remittanceCreationDate = remittanceCreationDate;
		this.paymentProcessorId = paymentProcessorId;
		this.reProcessStatus = reProcessStatus;
		this.etlRunId = etlRunId;
	}

	private Long paymentProcessorRemittanceId;
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateCreated;
	@JsonView({ Views.Extend.class, Views.Summary.class })
	private Long reconciliationStatusId;
	@JsonView({ Views.Extend.class, Views.Summary.class })
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private DateTime reconciliationDate;
	private String paymentMethod;
	private BigDecimal transactionAmount;
	private String transactionType;
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime transactionTime;
	private String accountId;
	private String application;
	private String processorTransactionId;
	private String merchantId;
	private String transactionSource;
	private String firstName;
	private String lastName;
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private DateTime remittanceCreationDate;
	@JsonView({ Views.Extend.class, Views.Summary.class })
	private Long paymentProcessorId;
	@JsonIgnore
	private String reProcessStatus;
	@JsonIgnore
	private Long etlRunId;
	// Fields added by dheeraj
	private DateTime createdDate;
	private String processorName; 
	private Long saleTransactionId; 
	private String saleTransactionType; 
	private String saleLegalEntityApp;
	private String saleAccountNumber; 
	private String saleApplicationTransactionId; 
	private String saleProcessorTransactionId;
	private String saleMerchantId; 
	private DateTime saleTransactionDateTime;
	private String saleCardNumberFirst6Char;
	private String saleCardNumberLast4Char; 
	private String saleCardType; 
	private BigDecimal saleAmount; 
	private Date saleExpiryDate;
	private String saleFirstName; 
	private String saleLastName; 
	private String saleAddress1; 
	private String saleAddress2; 
	private String saleCity;
	private String saleState; 
	private String salePostalCode; 
	private String saleCountry; 
	private Short saleTestMode; 
	private String saleToken;
	private Short saleTokenized; 
	private String salePaymentProcessorResponseCode;
	private String salePaymentProcessorResponseCodeDescription; 
	private String saleApprovalCode;
	private String saleInternalResponseCode; 
	private String saleInternalResponseDescription; 
	private String saleInternalStatusCode;
	private String saleInternalStatusDescription; 
	private String salePaymentProcessorStatusCode;
	private String salePaymentProcessorStatusCodeDescription; 
	private Long salePaymentProcessorRuleId;
	private Long saleRulePaymentProcessorId; 
	private String saleRuleCardType; 
	private BigDecimal saleRuleMaximumMonthlyAmount;
	private Short saleRuleNoMaximumMonthlyAmountFlag; 
	private Short saleRulePriority; 
	private String saleProcessUser;
	private String saleProcessorName; 
	private String saleApplication; 
	private String saleOrigin; 
	private String saleAccountPeriod;
	private String saleDesk; 
	private String saleInvoiceNumber; 
	private String saleUserDefinedField1; 
	private String saleUserDefinedField2;
	private String saleUserDefinedField3; 
	private DateTime saleCreatedDate; 
	private Integer saleIsVoided; 
	private Integer saleIsRefunded;
	private Long salePaymentProcessorInternalStatusCodeId; 
	private Long salePaymentProcessorInternalResponseCodeId;
	private Long saleReconciliationStatusId; 
	private DateTime saleReconciliationDate; 
	private Long saleBatchUploadId;
	private String Processor_Name; 
	private String MID; 
	private String ReconciliationStatus_ID;
	private String saleProcessor;
	private String saleAccountId;
	private BigDecimal saleChargeAmount;
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof PaymentProcessorRemittance)) {
			return false;
		}
		PaymentProcessorRemittance paymentProcessorRemittance = (PaymentProcessorRemittance) o;
		return paymentProcessorRemittanceId == paymentProcessorRemittance.paymentProcessorRemittanceId
				&& reconciliationStatusId == paymentProcessorRemittance.reconciliationStatusId
				&& Objects.equals(paymentMethod, paymentProcessorRemittance.paymentMethod)
				&& Objects.equals(processorTransactionId, paymentProcessorRemittance.processorTransactionId)
				&& paymentProcessorId == paymentProcessorRemittance.paymentProcessorId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(paymentProcessorRemittanceId, reconciliationStatusId, paymentMethod, processorTransactionId,
				paymentProcessorId);
	}

	public Long getPaymentProcessorRemittanceId() {
		return paymentProcessorRemittanceId;
	}

	public void setPaymentProcessorRemittanceId(Long paymentProcessorRemittanceId) {
		this.paymentProcessorRemittanceId = paymentProcessorRemittanceId;
	}

	public DateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(DateTime dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Long getReconciliationStatusId() {
		return reconciliationStatusId;
	}

	public void setReconciliationStatusId(Long reconciliationStatusId) {
		this.reconciliationStatusId = reconciliationStatusId;
	}

	public DateTime getReconciliationDate() {
		return reconciliationDate;
	}

	public void setReconciliationDate(DateTime reconciliationDate) {
		this.reconciliationDate = reconciliationDate;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public DateTime getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(DateTime transactionTime) {
		this.transactionTime = transactionTime;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getProcessorTransactionId() {
		return processorTransactionId;
	}

	public void setProcessorTransactionId(String processorTransactionId) {
		this.processorTransactionId = processorTransactionId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getTransactionSource() {
		return transactionSource;
	}

	public void setTransactionSource(String transactionSource) {
		this.transactionSource = transactionSource;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public DateTime getRemittanceCreationDate() {
		return remittanceCreationDate;
	}

	public void setRemittanceCreationDate(DateTime remittanceCreationDate) {
		this.remittanceCreationDate = remittanceCreationDate;
	}

	public Long getPaymentProcessorId() {
		return paymentProcessorId;
	}

	public void setPaymentProcessorId(Long paymentProcessorId) {
		this.paymentProcessorId = paymentProcessorId;
	}

	public String getReProcessStatus() {
		return reProcessStatus;
	}

	public void setReProcessStatus(String reProcessStatus) {
		this.reProcessStatus = reProcessStatus;
	}

	public Long getEtlRunId() {
		return etlRunId;
	}

	public void setEtlRunId(Long etlRunId) {
		this.etlRunId = etlRunId;
	}

	@Override
	public String getApplicationTransactionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DateTime getTransactionDateTime() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// setter and getters added by dheeraj
	public DateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(DateTime createdDate) {
		this.createdDate = createdDate;
	}

	public String getProcessorName() {
		return processorName;
	}

	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}

	public Long getSaleTransactionId() {
		return saleTransactionId;
	}

	public void setSaleTransactionId(Long saleTransactionId) {
		this.saleTransactionId = saleTransactionId;
	}

	public String getSaleTransactionType() {
		return saleTransactionType;
	}

	public void setSaleTransactionType(String saleTransactionType) {
		this.saleTransactionType = saleTransactionType;
	}

	public String getSaleLegalEntityApp() {
		return saleLegalEntityApp;
	}

	public void setSaleLegalEntityApp(String saleLegalEntityApp) {
		this.saleLegalEntityApp = saleLegalEntityApp;
	}

	public String getSaleAccountNumber() {
		return saleAccountNumber;
	}

	public void setSaleAccountNumber(String saleAccountNumber) {
		this.saleAccountNumber = saleAccountNumber;
	}

	public String getSaleApplicationTransactionId() {
		return saleApplicationTransactionId;
	}

	public void setSaleApplicationTransactionId(String saleApplicationTransactionId) {
		this.saleApplicationTransactionId = saleApplicationTransactionId;
	}

	public String getSaleProcessorTransactionId() {
		return saleProcessorTransactionId;
	}

	public void setSaleProcessorTransactionId(String saleProcessorTransactionId) {
		this.saleProcessorTransactionId = saleProcessorTransactionId;
	}

	public String getSaleMerchantId() {
		return saleMerchantId;
	}

	public void setSaleMerchantId(String saleMerchantId) {
		this.saleMerchantId = saleMerchantId;
	}

	public DateTime getSaleTransactionDateTime() {
		return saleTransactionDateTime;
	}

	public void setSaleTransactionDateTime(DateTime saleTransactionDateTime) {
		this.saleTransactionDateTime = saleTransactionDateTime;
	}

	public String getSaleCardNumberFirst6Char() {
		return saleCardNumberFirst6Char;
	}

	public void setSaleCardNumberFirst6Char(String saleCardNumberFirst6Char) {
		this.saleCardNumberFirst6Char = saleCardNumberFirst6Char;
	}

	public String getSaleCardNumberLast4Char() {
		return saleCardNumberLast4Char;
	}

	public void setSaleCardNumberLast4Char(String saleCardNumberLast4Char) {
		this.saleCardNumberLast4Char = saleCardNumberLast4Char;
	}

	public String getSaleCardType() {
		return saleCardType;
	}

	public void setSaleCardType(String saleCardType) {
		this.saleCardType = saleCardType;
	}

	public BigDecimal getSaleAmount() {
		return saleAmount;
	}

	public void setSaleAmount(BigDecimal saleAmount) {
		this.saleAmount = saleAmount;
	}

	public Date getSaleExpiryDate() {
		return saleExpiryDate;
	}

	public void setSaleExpiryDate(Date saleExpiryDate) {
		this.saleExpiryDate = saleExpiryDate;
	}

	public String getSaleFirstName() {
		return saleFirstName;
	}

	public void setSaleFirstName(String saleFirstName) {
		this.saleFirstName = saleFirstName;
	}

	public String getSaleLastName() {
		return saleLastName;
	}

	public void setSaleLastName(String saleLastName) {
		this.saleLastName = saleLastName;
	}

	public String getSaleAddress1() {
		return saleAddress1;
	}

	public void setSaleAddress1(String saleAddress1) {
		this.saleAddress1 = saleAddress1;
	}

	public String getSaleAddress2() {
		return saleAddress2;
	}

	public void setSaleAddress2(String saleAddress2) {
		this.saleAddress2 = saleAddress2;
	}

	public String getSaleCity() {
		return saleCity;
	}

	public void setSaleCity(String saleCity) {
		this.saleCity = saleCity;
	}

	public String getSaleState() {
		return saleState;
	}

	public void setSaleState(String saleState) {
		this.saleState = saleState;
	}

	public String getSalePostalCode() {
		return salePostalCode;
	}

	public void setSalePostalCode(String salePostalCode) {
		this.salePostalCode = salePostalCode;
	}

	public String getSaleCountry() {
		return saleCountry;
	}

	public void setSaleCountry(String saleCountry) {
		this.saleCountry = saleCountry;
	}

	public Short getSaleTestMode() {
		return saleTestMode;
	}

	public void setSaleTestMode(Short saleTestMode) {
		this.saleTestMode = saleTestMode;
	}

	public String getSaleToken() {
		return saleToken;
	}

	public void setSaleToken(String saleToken) {
		this.saleToken = saleToken;
	}

	public Short getSaleTokenized() {
		return saleTokenized;
	}

	public void setSaleTokenized(Short saleTokenized) {
		this.saleTokenized = saleTokenized;
	}

	public String getSalePaymentProcessorResponseCode() {
		return salePaymentProcessorResponseCode;
	}

	public void setSalePaymentProcessorResponseCode(String salePaymentProcessorResponseCode) {
		this.salePaymentProcessorResponseCode = salePaymentProcessorResponseCode;
	}

	public String getSalePaymentProcessorResponseCodeDescription() {
		return salePaymentProcessorResponseCodeDescription;
	}

	public void setSalePaymentProcessorResponseCodeDescription(String salePaymentProcessorResponseCodeDescription) {
		this.salePaymentProcessorResponseCodeDescription = salePaymentProcessorResponseCodeDescription;
	}

	public String getSaleApprovalCode() {
		return saleApprovalCode;
	}

	public void setSaleApprovalCode(String saleApprovalCode) {
		this.saleApprovalCode = saleApprovalCode;
	}

	public String getSaleInternalResponseCode() {
		return saleInternalResponseCode;
	}

	public void setSaleInternalResponseCode(String saleInternalResponseCode) {
		this.saleInternalResponseCode = saleInternalResponseCode;
	}

	public String getSaleInternalResponseDescription() {
		return saleInternalResponseDescription;
	}

	public void setSaleInternalResponseDescription(String saleInternalResponseDescription) {
		this.saleInternalResponseDescription = saleInternalResponseDescription;
	}

	public String getSaleInternalStatusCode() {
		return saleInternalStatusCode;
	}

	public void setSaleInternalStatusCode(String saleInternalStatusCode) {
		this.saleInternalStatusCode = saleInternalStatusCode;
	}

	public String getSaleInternalStatusDescription() {
		return saleInternalStatusDescription;
	}

	public void setSaleInternalStatusDescription(String saleInternalStatusDescription) {
		this.saleInternalStatusDescription = saleInternalStatusDescription;
	}

	public String getSalePaymentProcessorStatusCode() {
		return salePaymentProcessorStatusCode;
	}

	public void setSalePaymentProcessorStatusCode(String salePaymentProcessorStatusCode) {
		this.salePaymentProcessorStatusCode = salePaymentProcessorStatusCode;
	}

	public String getSalePaymentProcessorStatusCodeDescription() {
		return salePaymentProcessorStatusCodeDescription;
	}

	public void setSalePaymentProcessorStatusCodeDescription(String salePaymentProcessorStatusCodeDescription) {
		this.salePaymentProcessorStatusCodeDescription = salePaymentProcessorStatusCodeDescription;
	}

	public Long getSalePaymentProcessorRuleId() {
		return salePaymentProcessorRuleId;
	}

	public void setSalePaymentProcessorRuleId(Long salePaymentProcessorRuleId) {
		this.salePaymentProcessorRuleId = salePaymentProcessorRuleId;
	}

	public Long getSaleRulePaymentProcessorId() {
		return saleRulePaymentProcessorId;
	}

	public void setSaleRulePaymentProcessorId(Long saleRulePaymentProcessorId) {
		this.saleRulePaymentProcessorId = saleRulePaymentProcessorId;
	}

	public String getSaleRuleCardType() {
		return saleRuleCardType;
	}

	public void setSaleRuleCardType(String saleRuleCardType) {
		this.saleRuleCardType = saleRuleCardType;
	}

	public BigDecimal getSaleRuleMaximumMonthlyAmount() {
		return saleRuleMaximumMonthlyAmount;
	}

	public void setSaleRuleMaximumMonthlyAmount(BigDecimal saleRuleMaximumMonthlyAmount) {
		this.saleRuleMaximumMonthlyAmount = saleRuleMaximumMonthlyAmount;
	}

	public Short getSaleRuleNoMaximumMonthlyAmountFlag() {
		return saleRuleNoMaximumMonthlyAmountFlag;
	}

	public void setSaleRuleNoMaximumMonthlyAmountFlag(Short saleRuleNoMaximumMonthlyAmountFlag) {
		this.saleRuleNoMaximumMonthlyAmountFlag = saleRuleNoMaximumMonthlyAmountFlag;
	}

	public Short getSaleRulePriority() {
		return saleRulePriority;
	}

	public void setSaleRulePriority(Short saleRulePriority) {
		this.saleRulePriority = saleRulePriority;
	}

	public String getSaleProcessUser() {
		return saleProcessUser;
	}

	public void setSaleProcessUser(String saleProcessUser) {
		this.saleProcessUser = saleProcessUser;
	}

	public String getSaleProcessorName() {
		return saleProcessorName;
	}

	public void setSaleProcessorName(String saleProcessorName) {
		this.saleProcessorName = saleProcessorName;
	}

	public String getSaleApplication() {
		return saleApplication;
	}

	public void setSaleApplication(String saleApplication) {
		this.saleApplication = saleApplication;
	}

	public String getSaleOrigin() {
		return saleOrigin;
	}

	public void setSaleOrigin(String saleOrigin) {
		this.saleOrigin = saleOrigin;
	}

	public String getSaleAccountPeriod() {
		return saleAccountPeriod;
	}

	public void setSaleAccountPeriod(String saleAccountPeriod) {
		this.saleAccountPeriod = saleAccountPeriod;
	}

	public String getSaleDesk() {
		return saleDesk;
	}

	public void setSaleDesk(String saleDesk) {
		this.saleDesk = saleDesk;
	}

	public String getSaleInvoiceNumber() {
		return saleInvoiceNumber;
	}

	public void setSaleInvoiceNumber(String saleInvoiceNumber) {
		this.saleInvoiceNumber = saleInvoiceNumber;
	}

	public String getSaleUserDefinedField1() {
		return saleUserDefinedField1;
	}

	public void setSaleUserDefinedField1(String saleUserDefinedField1) {
		this.saleUserDefinedField1 = saleUserDefinedField1;
	}

	public String getSaleUserDefinedField2() {
		return saleUserDefinedField2;
	}

	public void setSaleUserDefinedField2(String saleUserDefinedField2) {
		this.saleUserDefinedField2 = saleUserDefinedField2;
	}

	public String getSaleUserDefinedField3() {
		return saleUserDefinedField3;
	}

	public void setSaleUserDefinedField3(String saleUserDefinedField3) {
		this.saleUserDefinedField3 = saleUserDefinedField3;
	}

	public DateTime getSaleCreatedDate() {
		return saleCreatedDate;
	}

	public void setSaleCreatedDate(DateTime saleCreatedDate) {
		this.saleCreatedDate = saleCreatedDate;
	}

	public Integer getSaleIsVoided() {
		return saleIsVoided;
	}

	public void setSaleIsVoided(Integer saleIsVoided) {
		this.saleIsVoided = saleIsVoided;
	}

	public Integer getSaleIsRefunded() {
		return saleIsRefunded;
	}

	public void setSaleIsRefunded(Integer saleIsRefunded) {
		this.saleIsRefunded = saleIsRefunded;
	}

	public Long getSalePaymentProcessorInternalStatusCodeId() {
		return salePaymentProcessorInternalStatusCodeId;
	}

	public void setSalePaymentProcessorInternalStatusCodeId(Long salePaymentProcessorInternalStatusCodeId) {
		this.salePaymentProcessorInternalStatusCodeId = salePaymentProcessorInternalStatusCodeId;
	}

	public Long getSalePaymentProcessorInternalResponseCodeId() {
		return salePaymentProcessorInternalResponseCodeId;
	}

	public void setSalePaymentProcessorInternalResponseCodeId(Long salePaymentProcessorInternalResponseCodeId) {
		this.salePaymentProcessorInternalResponseCodeId = salePaymentProcessorInternalResponseCodeId;
	}

	public Long getSaleReconciliationStatusId() {
		return saleReconciliationStatusId;
	}

	public void setSaleReconciliationStatusId(Long saleReconciliationStatusId) {
		this.saleReconciliationStatusId = saleReconciliationStatusId;
	}

	public DateTime getSaleReconciliationDate() {
		return saleReconciliationDate;
	}

	public void setSaleReconciliationDate(DateTime saleReconciliationDate) {
		this.saleReconciliationDate = saleReconciliationDate;
	}

	public Long getSaleBatchUploadId() {
		return saleBatchUploadId;
	}

	public void setSaleBatchUploadId(Long saleBatchUploadId) {
		this.saleBatchUploadId = saleBatchUploadId;
	}

	public String getProcessor_Name() {
		return Processor_Name;
	}

	public void setProcessor_Name(String processor_Name) {
		Processor_Name = processor_Name;
	}

	public String getMID() {
		return MID;
	}

	public void setMID(String mID) {
		MID = mID;
	}

	public String getReconciliationStatus_ID() {
		return ReconciliationStatus_ID;
	}

	public void setReconciliationStatus_ID(String reconciliationStatus_ID) {
		ReconciliationStatus_ID = reconciliationStatus_ID;
	}

	public String getSaleProcessor() {
		return saleProcessor;
	}

	public void setSaleProcessor(String saleProcessor) {
		this.saleProcessor = saleProcessor;
	}

	public String getSaleAccountId() {
		return saleAccountId;
	}

	public void setSaleAccountId(String saleAccountId) {
		this.saleAccountId = saleAccountId;
	}

	public BigDecimal getSaleChargeAmount() {
		return saleChargeAmount;
	}

	public void setSaleChargeAmount(BigDecimal saleChargeAmount) {
		this.saleChargeAmount = saleChargeAmount;
	}
}
