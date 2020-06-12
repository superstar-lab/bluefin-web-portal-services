package com.mcmcg.ico.bluefin.rest.resource;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mcmcg.ico.bluefin.model.BinDBDetails;

public class TransactionPageImpl<T> extends PageImpl<T> {

	private static final long serialVersionUID = 1409745156033130199L;
	
	@JsonProperty("content-bindb-details")
	private transient List<BinDBDetails> binDBDetails = new ArrayList<>();
	
	public TransactionPageImpl(List<T> content, Pageable pageable, long total,List<BinDBDetails> binDBDetails){
		super(content, pageable, total);
		this.binDBDetails = binDBDetails;
	}

	public List<BinDBDetails> getBinDBDetails() {
		return binDBDetails;
	}

	public void setBinDBDetails(List<BinDBDetails> binDBDetails) {
		this.binDBDetails = binDBDetails;
	}
	
	@Override
	 public boolean equals(Object  obj) {	  
		 return super.equals(obj);
	  }
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
}
