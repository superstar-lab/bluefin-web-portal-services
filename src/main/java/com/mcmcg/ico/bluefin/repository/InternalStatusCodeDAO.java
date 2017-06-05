package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.InternalStatusCode;

public interface InternalStatusCodeDAO {

	public InternalStatusCode findByInternalStatusCodeAndTransactionTypeName(String internalStatusCode,
            String transactionTypeName);

    public List<InternalStatusCode> findByTransactionTypeNameOrderByInternalStatusCodeAsc(
            final String transactionTypeName);

    public InternalStatusCode save(InternalStatusCode internalStatusCode);
    
    public InternalStatusCode update(InternalStatusCode internalStatusCode);
    
    public InternalStatusCode findOne(Long internalStatusCodeId);
    
    public void delete(Long internalStatusCodeId);
    
    public InternalStatusCode findOneWithChilds(Long internalStatusCodeId);
}
