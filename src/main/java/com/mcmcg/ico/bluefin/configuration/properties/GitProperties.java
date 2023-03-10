package com.mcmcg.ico.bluefin.configuration.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GitProperties {

    private String branch;
    private final GitBuildProperties build;
    private final GitCommitProperties commit;
    private final boolean dirty;
    private final String remoteOriginUrl;
    private final String tags;

    @Autowired
    public GitProperties(GitBuildProperties build, GitCommitProperties commit,
            @Value("${bluefin.wp.services.git.branch}") String branch,
            @Value("${bluefin.wp.services.git.dirty}") boolean dirty,
            @Value("${bluefin.wp.services.git.remote.origin.url}") String remoteOriginUrl,
            @Value("${bluefin.wp.services.git.tags}") String tags) {
        this.branch = branch;
        this.build = build;
        this.commit = commit;
        this.dirty = dirty;
        this.remoteOriginUrl = remoteOriginUrl;
        this.tags = tags;
    }

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public GitBuildProperties getBuild() {
		return build;
	}

	public GitCommitProperties getCommit() {
		return commit;
	}

	public boolean isDirty() {
		return dirty;
	}

	public String getRemoteOriginUrl() {
		return remoteOriginUrl;
	}

	public String getTags() {
		return tags;
	}
    
    
}
