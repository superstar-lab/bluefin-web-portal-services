package com.mcmcg.ico.bluefin.rest.resource;

import java.sql.Time;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.mcmcg.ico.bluefin.model.PaymentProcessor;

public class BasicPaymentProcessorResource {
    private static final long serialVersionUID = -602175101416750669L;

    @NotBlank(message = "Please provide a processor name")
    @Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field processor name must be alphanumeric")
    private String processorName;
    private Short isActive = 0;
    private Time remitTransactionOpenTime;
    private Time remitTransactionCloseTime;
    private String lastModifiedBy;
    
    public PaymentProcessor toPaymentProcessor() {
    	PaymentProcessor paymentProcessor = new PaymentProcessor();
        paymentProcessor.setProcessorName(processorName);
        paymentProcessor.setIsActive(isActive);
        paymentProcessor.setRemitTransactionOpenTime(remitTransactionOpenTime);
        paymentProcessor.setRemitTransactionCloseTime(remitTransactionCloseTime);
        paymentProcessor.setCreatedDate(DateTime.now(DateTimeZone.UTC));
        paymentProcessor.setModifiedDate(DateTime.now(DateTimeZone.UTC));
        paymentProcessor.setLastModifiedBy(lastModifiedBy);
        return paymentProcessor;
    }

	public String getProcessorName() {
		return processorName;
	}

	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}

	public Short getIsActive() {
		return isActive;
	}

	public void setIsActive(Short isActive) {
		this.isActive = isActive;
	}

	public Time getRemitTransactionOpenTime() {
		return remitTransactionOpenTime;
	}

	public void setRemitTransactionOpenTime(Time remitTransactionOpenTime) {
		this.remitTransactionOpenTime = remitTransactionOpenTime;
	}

	public Time getRemitTransactionCloseTime() {
		return remitTransactionCloseTime;
	}

	public void setRemitTransactionCloseTime(Time remitTransactionCloseTime) {
		this.remitTransactionCloseTime = remitTransactionCloseTime;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
    
    
}
