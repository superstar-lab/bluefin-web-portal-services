package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

public class Application implements Serializable {

	private static final long serialVersionUID = 1619568547266213593L;

	private Long applicationId;
	private String applicationName;

	public Application() {
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Application)) {
			return false;
		}
		Application application = (Application) o;
		return applicationId == application.applicationId
				&& Objects.equals(applicationName, application.applicationName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(applicationId, applicationName);
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
