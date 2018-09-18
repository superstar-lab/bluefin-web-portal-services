package com.mcmcg.ico.bluefin.rest.resource;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mcmcg.ico.bluefin.model.BinDBDetails;

import lombok.Data;

@Data
public class TransactionPageImpl<T> extends PageImpl<T> {
	
	@JsonProperty("content-bindb-details")
	private List<BinDBDetails> binDBDetails = new ArrayList<>();
	
	public TransactionPageImpl(List<T> content, Pageable pageable, long total,List<BinDBDetails> binDBDetails){
		super(content, pageable, total);
		this.binDBDetails = binDBDetails;
	}
	
}
