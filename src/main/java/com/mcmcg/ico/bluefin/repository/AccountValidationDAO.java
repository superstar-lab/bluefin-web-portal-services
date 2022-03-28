package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.AccountValidation;
import org.springframework.data.domain.PageRequest;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface AccountValidationDAO {
    public List<AccountValidation> findAll();

    public Map<String, Object> findAllFilter(String startDate, String endDate, PageRequest page) throws ParseException;

}
