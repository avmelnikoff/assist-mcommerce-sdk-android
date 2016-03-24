package ru.assisttech.sdk.storage;

import java.util.Date;

import ru.assisttech.sdk.AssistPaymentData.Currency;
import ru.assisttech.sdk.AssistResult;

public class AssistTransactionFilter {
	
	private Date dateBegin;
	private Date dateEnd;
	private AssistResult.OrderState oState;
	private String amountMin;
	private String amountMax;
	private Currency currency;
	
	public Date getDateBegin() {
		return dateBegin;
	}
	
	public Date getDateEnd() {
		return dateEnd;
	}
	
	public AssistResult.OrderState getOrderState() {
		return oState;
	}
	
	public String getAmountMin() {
		return amountMin;
	}
	
	public String getAmountMax() {
		return amountMax;
	}
	
	public Currency getCurrency() {
		return currency;
	}
	
	public void setDateBegin(Date value) {
		dateBegin = value;
	}
	
	public void setDateEnd(Date value) {
		dateEnd = value;
	}
	
	public void setOrderState(AssistResult.OrderState value) {
		oState = value;
	}
	
	public void setAmountMin(String value) {
		amountMin = value;
	}
	
	public void setAmountMax(String value) {
		amountMax = value;
	}
	
	public void setCurrency(Currency value) {
		currency = value;
	}

	public void reset() {
        dateBegin = null;
        dateEnd = null;
        oState = null;
        amountMin = null;
        amountMax = null;
        currency = null;
	}
}