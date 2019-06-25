package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.mcmcg.ico.bluefin.rest.resource.Views;

public class SaleTransaction extends CommonTransaction implements Serializable {

	private static final long serialVersionUID = 6953410227212475805L;
	private static final String CARD_MASK = "XXXX-XXXX-XXXX-";

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private Long saleTransactionId;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String firstName;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String lastName;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String processUser;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String transactionType;

	@JsonView(Views.Extend.class)
	private String address1;

	@JsonView(Views.Extend.class)
	private String address2;

	@JsonView(Views.Extend.class)
	private String city;

	@JsonView(Views.Extend.class)
	private String state;

	@JsonView(Views.Extend.class)
	private String postalCode;

	@JsonView(Views.Extend.class)
	private String country;

	@JsonView(Views.Extend.class)
	private String cardNumberFirst6Char;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String cardNumberLast4Char;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String cardType;

	@JsonView(Views.Extend.class)
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/yy")
	private Date expiryDate = new Date();

	@JsonView(Views.Extend.class)
	private String token;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	@JsonProperty("amount")
	private BigDecimal chargeAmount;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	@JsonProperty("legalEntity")
	private String legalEntityApp;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	@JsonProperty("accountNumber")
	private String accountId;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String applicationTransactionId;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String merchantId;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	@JsonProperty("processorName")
	private String processor;

	@JsonView(Views.Extend.class)
	private String application;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String origin;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String processorTransactionId;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime transactionDateTime = new DateTime();

	@JsonView(Views.Extend.class)
	private Short testMode;

	@JsonView(Views.Extend.class)
	private String approvalCode;

	@JsonIgnore
	private Short tokenized;

	@JsonView({Views.Extend.class, Views.Summary.class})
	private String paymentProcessorStatusCode;

	@JsonView({Views.Extend.class, Views.Summary.class})
	private String paymentProcessorStatusCodeDescription;

	@JsonView({Views.Extend.class, Views.Summary.class})
	@JsonProperty("processorResponseCode")
	private String paymentProcessorResponseCode;

	@JsonView({Views.Extend.class, Views.Summary.class})
	@JsonProperty("processorResponseCodeDescription")
	private String paymentProcessorResponseCodeDescription;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String internalStatusCode;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String internalStatusDescription;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String internalResponseCode;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String internalResponseDescription;

	@JsonView(Views.Extend.class)
	private Long paymentProcessorInternalStatusCodeId;

	@JsonView(Views.Extend.class)
	private Long paymentProcessorInternalResponseCodeId;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateCreated = new DateTime();

	@JsonView(Views.Extend.class)
	private Long paymentProcessorRuleId;

	@JsonView(Views.Extend.class)
	private Long rulePaymentProcessorId;

	@JsonView(Views.Extend.class)
	private String ruleCardType;

	@JsonView(Views.Extend.class)
	private BigDecimal ruleMaximumMonthlyAmount;

	@JsonView(Views.Extend.class)
	private Short ruleNoMaximumMonthlyAmountFlag;

	@JsonView(Views.Extend.class)
	private Short rulePriority;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String accountPeriod;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String desk;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String invoiceNumber;

	@JsonView(Views.Extend.class)
	private String userDefinedField1;

	@JsonView(Views.Extend.class)
	private String userDefinedField2;

	@JsonView(Views.Extend.class)
	private String userDefinedField3;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private Long reconciliationStatusId;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime reconciliationDate;
	/*private DateTime reconciliationDate = new DateTime();*/

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private Long batchUploadId;

	@JsonIgnore
	private Long etlRunId;

	// Not in the table.
	private Integer isVoided = 0;

	// Not in the table.
	private Integer isRefunded = 0;

	@JsonView({ Views.Extend.class, Views.Summary.class })
	private String paymentFrequency;
	
	public SaleTransaction() {
		// Default Constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SaleTransaction)) {
			return false;
		}
		SaleTransaction saleTransaction = (SaleTransaction) o;
		boolean idEq = saleTransactionId == saleTransaction.saleTransactionId;
		boolean firstAndLastNmEq = Objects.equals(firstName, saleTransaction.firstName)
				&& Objects.equals(lastName, saleTransaction.lastName);
		boolean processUserAndTranTypeEq = Objects.equals(processUser, saleTransaction.processUser)
				&& Objects.equals(transactionType, saleTransaction.transactionType);
		return idEq
				&& firstAndLastNmEq
				&& processUserAndTranTypeEq;
	}

	@Override
	public int hashCode() {
		return Objects.hash(saleTransactionId, firstName, lastName, processUser, transactionType);
	}
	public static String getCardMask() {
		return CARD_MASK;
	}
	
	public String getPaymentFrequency() {
        return PaymentFrequency.getPaymentFrequency(origin).toString();
    }
	
	public String getCardNumberFirst6Char(){
		if (this.cardNumberFirst6Char == null) {
			this.cardNumberFirst6Char = "";
		}
		return this.cardNumberFirst6Char;
	}
	
	public String getCardNumberLast4Char(){
		if (this.cardNumberLast4Char == null) {
			this.cardNumberLast4Char = "";
		}
		return this.cardNumberLast4Char;
	}

	public Long getSaleTransactionId() {
		return saleTransactionId;
	}

	public void setSaleTransactionId(Long saleTransactionId) {
		this.saleTransactionId = saleTransactionId;
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

	public String getProcessUser() {
		return processUser;
	}

	public void setProcessUser(String processUser) {
		this.processUser = processUser;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public BigDecimal getChargeAmount() {
		return chargeAmount;
	}

	public void setChargeAmount(BigDecimal chargeAmount) {
		this.chargeAmount = chargeAmount;
	}

	public String getLegalEntityApp() {
		return legalEntityApp;
	}

	public void setLegalEntityApp(String legalEntityApp) {
		this.legalEntityApp = legalEntityApp;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getApplicationTransactionId() {
		return applicationTransactionId;
	}

	public void setApplicationTransactionId(String applicationTransactionId) {
		this.applicationTransactionId = applicationTransactionId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getProcessor() {
		return processor;
	}

	public void setProcessor(String processor) {
		this.processor = processor;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getProcessorTransactionId() {
		return processorTransactionId;
	}

	public void setProcessorTransactionId(String processorTransactionId) {
		this.processorTransactionId = processorTransactionId;
	}

	public DateTime getTransactionDateTime() {
		return transactionDateTime;
	}

	public void setTransactionDateTime(DateTime transactionDateTime) {
		this.transactionDateTime = transactionDateTime;
	}

	public Short getTestMode() {
		return testMode;
	}

	public void setTestMode(Short testMode) {
		this.testMode = testMode;
	}

	public String getApprovalCode() {
		return approvalCode;
	}

	public void setApprovalCode(String approvalCode) {
		this.approvalCode = approvalCode;
	}

	public Short getTokenized() {
		return tokenized;
	}

	public void setTokenized(Short tokenized) {
		this.tokenized = tokenized;
	}

	public String getPaymentProcessorStatusCode() {
		return paymentProcessorStatusCode;
	}

	public void setPaymentProcessorStatusCode(String paymentProcessorStatusCode) {
		this.paymentProcessorStatusCode = paymentProcessorStatusCode;
	}

	public String getPaymentProcessorStatusCodeDescription() {
		return paymentProcessorStatusCodeDescription;
	}

	public void setPaymentProcessorStatusCodeDescription(String paymentProcessorStatusCodeDescription) {
		this.paymentProcessorStatusCodeDescription = paymentProcessorStatusCodeDescription;
	}

	public String getPaymentProcessorResponseCode() {
		return paymentProcessorResponseCode;
	}

	public void setPaymentProcessorResponseCode(String paymentProcessorResponseCode) {
		this.paymentProcessorResponseCode = paymentProcessorResponseCode;
	}

	public String getPaymentProcessorResponseCodeDescription() {
		return paymentProcessorResponseCodeDescription;
	}

	public void setPaymentProcessorResponseCodeDescription(String paymentProcessorResponseCodeDescription) {
		this.paymentProcessorResponseCodeDescription = paymentProcessorResponseCodeDescription;
	}

	public String getInternalStatusCode() {
		return internalStatusCode;
	}

	public void setInternalStatusCode(String internalStatusCode) {
		this.internalStatusCode = internalStatusCode;
	}

	public String getInternalStatusDescription() {
		return internalStatusDescription;
	}

	public void setInternalStatusDescription(String internalStatusDescription) {
		this.internalStatusDescription = internalStatusDescription;
	}

	public String getInternalResponseCode() {
		return internalResponseCode;
	}

	public void setInternalResponseCode(String internalResponseCode) {
		this.internalResponseCode = internalResponseCode;
	}

	public String getInternalResponseDescription() {
		return internalResponseDescription;
	}

	public void setInternalResponseDescription(String internalResponseDescription) {
		this.internalResponseDescription = internalResponseDescription;
	}

	public Long getPaymentProcessorInternalStatusCodeId() {
		return paymentProcessorInternalStatusCodeId;
	}

	public void setPaymentProcessorInternalStatusCodeId(Long paymentProcessorInternalStatusCodeId) {
		this.paymentProcessorInternalStatusCodeId = paymentProcessorInternalStatusCodeId;
	}

	public Long getPaymentProcessorInternalResponseCodeId() {
		return paymentProcessorInternalResponseCodeId;
	}

	public void setPaymentProcessorInternalResponseCodeId(Long paymentProcessorInternalResponseCodeId) {
		this.paymentProcessorInternalResponseCodeId = paymentProcessorInternalResponseCodeId;
	}

	public DateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(DateTime dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Long getPaymentProcessorRuleId() {
		return paymentProcessorRuleId;
	}

	public void setPaymentProcessorRuleId(Long paymentProcessorRuleId) {
		this.paymentProcessorRuleId = paymentProcessorRuleId;
	}

	public Long getRulePaymentProcessorId() {
		return rulePaymentProcessorId;
	}

	public void setRulePaymentProcessorId(Long rulePaymentProcessorId) {
		this.rulePaymentProcessorId = rulePaymentProcessorId;
	}

	public String getRuleCardType() {
		return ruleCardType;
	}

	public void setRuleCardType(String ruleCardType) {
		this.ruleCardType = ruleCardType;
	}

	public BigDecimal getRuleMaximumMonthlyAmount() {
		return ruleMaximumMonthlyAmount;
	}

	public void setRuleMaximumMonthlyAmount(BigDecimal ruleMaximumMonthlyAmount) {
		this.ruleMaximumMonthlyAmount = ruleMaximumMonthlyAmount;
	}

	public Short getRuleNoMaximumMonthlyAmountFlag() {
		return ruleNoMaximumMonthlyAmountFlag;
	}

	public void setRuleNoMaximumMonthlyAmountFlag(Short ruleNoMaximumMonthlyAmountFlag) {
		this.ruleNoMaximumMonthlyAmountFlag = ruleNoMaximumMonthlyAmountFlag;
	}

	public Short getRulePriority() {
		return rulePriority;
	}

	public void setRulePriority(Short rulePriority) {
		this.rulePriority = rulePriority;
	}

	public String getAccountPeriod() {
		return accountPeriod;
	}

	public void setAccountPeriod(String accountPeriod) {
		this.accountPeriod = accountPeriod;
	}

	public String getDesk() {
		return desk;
	}

	public void setDesk(String desk) {
		this.desk = desk;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getUserDefinedField1() {
		return userDefinedField1;
	}

	public void setUserDefinedField1(String userDefinedField1) {
		this.userDefinedField1 = userDefinedField1;
	}

	public String getUserDefinedField2() {
		return userDefinedField2;
	}

	public void setUserDefinedField2(String userDefinedField2) {
		this.userDefinedField2 = userDefinedField2;
	}

	public String getUserDefinedField3() {
		return userDefinedField3;
	}

	public void setUserDefinedField3(String userDefinedField3) {
		this.userDefinedField3 = userDefinedField3;
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

	public Long getBatchUploadId() {
		return batchUploadId;
	}

	public void setBatchUploadId(Long batchUploadId) {
		this.batchUploadId = batchUploadId;
	}

	public Long getEtlRunId() {
		return etlRunId;
	}

	public void setEtlRunId(Long etlRunId) {
		this.etlRunId = etlRunId;
	}

	public Integer getIsVoided() {
		return isVoided;
	}

	public void setIsVoided(Integer isVoided) {
		this.isVoided = isVoided;
	}

	public Integer getIsRefunded() {
		return isRefunded;
	}

	public void setIsRefunded(Integer isRefunded) {
		this.isRefunded = isRefunded;
	}

	public void setCardNumberFirst6Char(String cardNumberFirst6Char) {
		this.cardNumberFirst6Char = cardNumberFirst6Char;
	}

	public void setCardNumberLast4Char(String cardNumberLast4Char) {
		this.cardNumberLast4Char = cardNumberLast4Char;
	}

	public void setPaymentFrequency(String paymentFrequency) {
		this.paymentFrequency = paymentFrequency;
	}
	
	
}
