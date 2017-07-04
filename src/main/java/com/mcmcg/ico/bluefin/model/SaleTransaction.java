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

import lombok.Data;

@Data
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

	@JsonView(Views.Extend.class)
	private String paymentProcessorStatusCode;

	@JsonView(Views.Extend.class)
	private String paymentProcessorStatusCodeDescription;

	@JsonView(Views.Extend.class)
	@JsonProperty("paymentProcessorResponseCode")
	private String paymentProcessorResponseCode;

	@JsonView(Views.Extend.class)
	@JsonProperty("paymentProcessorResponseCodeDescription")
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
	private DateTime reconciliationDate = new DateTime();

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
}
