package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;

public class Application implements Serializable {

	private static final long serialVersionUID = 1619568547266213593L;
	private Long applicationId;
	private String applicationName;

	public Application() {
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
}
