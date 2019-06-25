package com.mcmcg.ico.bluefin.configuration.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {
    private final String artifact;
    private final String name;
    private final String description;
    private final String version;
    private final GitProperties gitProperties;

    @Autowired
    public ApplicationProperties(@Value("${bluefin.wp.services.build.artifact}") String artifact,
            @Value("${bluefin.wp.services.build.name}") String name,
            @Value("${bluefin.wp.services.build.description}") String description,
            @Value("${bluefin.wp.services.build.version}") String version, GitProperties gitProperties) {
        this.artifact = artifact;
        this.name = name;
        this.description = description;
        this.version = version;
        this.gitProperties = gitProperties;
    }

	public String getArtifact() {
		return artifact;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getVersion() {
		return version;
	}

	public GitProperties getGitProperties() {
		return gitProperties;
	}
    
    
}
