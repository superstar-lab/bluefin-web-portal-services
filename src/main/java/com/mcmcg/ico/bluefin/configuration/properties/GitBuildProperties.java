package com.mcmcg.ico.bluefin.configuration.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GitBuildProperties {

    private final String time;
    private final String userEmail;
    private final String userName;

    @Autowired
    public GitBuildProperties(@Value("${bluefin.wp.services.git.build.time}") String time,
            @Value("${bluefin.wp.services.git.build.user.email}") String userEmail,
            @Value("${bluefin.wp.services.git.build.user.name}") String userName) {
        this.time = time;
        this.userEmail = userEmail;
        this.userName = userName;
    }

	public String getTime() {
		return time;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public String getUserName() {
		return userName;
	}
    
    
}
