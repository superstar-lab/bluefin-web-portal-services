package com.mcmcg.ico.bluefin.service;

import com.mcmcg.ico.bluefin.model.AccountValidation;
import com.mcmcg.ico.bluefin.repository.AccountValidationDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

@Service
public class AccountValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountValidationService.class);
    private static final String FAILEDTOPROCESSDATEFORMATMSG = "Unable to process find transaction, due an error with date formatting";
    @Autowired
    private AccountValidationDAO accountValidationDAO;

    public List<AccountValidation> getAccountValidations() {
        return accountValidationDAO.findAll();
    }

    public Map<String, Object> getAccountValidationFilter(String search, PageRequest paging) {
        Map<String, Object> result;
        try {
            result = accountValidationDAO.findAllFilter(search, paging);
        } catch (ParseException e) {
            throw new CustomNotFoundException(FAILEDTOPROCESSDATEFORMATMSG);
        }

        LOGGER.debug("result :={} ",result);
        return result;
    }
}
