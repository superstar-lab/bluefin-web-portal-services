package com.mcmcg.ico.bluefin.configuration.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GitCommitProperties {

    private final String id;
    private final String idAbbrev;
    private final String shortMessage;
    private final String time;
    private final String userEmail;
    private final String userName;

    @Autowired
    public GitCommitProperties(@Value("${bluefin.wp.services.git.commit.id}") String id,
            @Value("${bluefin.wp.services.git.commit.id.abbrev}") String idAbbrev,
            @Value("${bluefin.wp.services.git.commit.message.short}") String shortMessage,
            @Value("${bluefin.wp.services.git.commit.time}") String time,
            @Value("${bluefin.wp.services.git.commit.user.email}") String userEmail,
            @Value("${bluefin.wp.services.git.commit.user.name}") String userName) {
        this.id = id;
        this.idAbbrev = idAbbrev;
        this.shortMessage = shortMessage;
        this.time = time;
        this.userEmail = userEmail;
        this.userName = userName;
    }
}
