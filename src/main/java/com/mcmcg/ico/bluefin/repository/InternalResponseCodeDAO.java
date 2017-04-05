package com.mcmcg.ico.bluefin.repository;

import java.util.List;

public interface InternalResponseCodeDAO {

	public com.mcmcg.ico.bluefin.model.InternalResponseCode findByInternalResponseCodeAndTransactionTypeName(String internalResponseCode,
			String transactionTypeName) ;

	public List<com.mcmcg.ico.bluefin.model.InternalResponseCode> findByTransactionTypeNameOrderByInternalResponseCodeAsc(
			String transactionTypeName) ;

	public com.mcmcg.ico.bluefin.model.InternalResponseCode save(com.mcmcg.ico.bluefin.model.InternalResponseCode internalResponseCode);

	public com.mcmcg.ico.bluefin.model.InternalResponseCode findOne(long internalResponseCodeId);

	public void delete(com.mcmcg.ico.bluefin.model.InternalResponseCode internalResponseCode) ;

	public List<com.mcmcg.ico.bluefin.model.InternalResponseCode> findAll();
	
	public com.mcmcg.ico.bluefin.model.InternalResponseCode update(com.mcmcg.ico.bluefin.model.InternalResponseCode internalResponseCode);

	public com.mcmcg.ico.bluefin.model.InternalResponseCode findOneWithChilds(Long internalResponseCodeId);
}
