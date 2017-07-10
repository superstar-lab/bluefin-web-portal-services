package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.InternalResponseCode;

public interface InternalResponseCodeDAO {

	public InternalResponseCode findByInternalResponseCodeAndTransactionTypeName(String internalResponseCode,
			String transactionTypeName) ;

	public List<InternalResponseCode> findByTransactionTypeNameOrderByInternalResponseCodeAsc(
			String transactionTypeName) ;

	public InternalResponseCode save(InternalResponseCode internalResponseCode);

	public InternalResponseCode findOne(long internalResponseCodeId);

	public void delete(InternalResponseCode internalResponseCode) ;

	public List<InternalResponseCode> findAll();
	
	public InternalResponseCode update(InternalResponseCode internalResponseCode);

	public InternalResponseCode findOneWithChilds(Long internalResponseCodeId);
}
