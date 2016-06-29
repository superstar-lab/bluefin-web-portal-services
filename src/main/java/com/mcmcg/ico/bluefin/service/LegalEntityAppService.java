package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.jpa.LegalEntityAppRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;

@Service
public class LegalEntityAppService {

    @Autowired
    private LegalEntityAppRepository legalEntityAppRepository;
    @Autowired
    private UserRepository userRepository;

    public List<LegalEntityApp> getLegalEntities(String userName) {
        User user = userRepository.findByUsername(userName);

        if (user == null) {
            return new ArrayList<LegalEntityApp>();
        }

        List<Long> listOfIds = user.getLegalEntities().stream()
                .map(userLegalEntity -> userLegalEntity.getLegalEntityApp().getLegalEntityAppId())
                .collect(Collectors.toList());

        return legalEntityAppRepository.findAll(listOfIds);
    }
}
