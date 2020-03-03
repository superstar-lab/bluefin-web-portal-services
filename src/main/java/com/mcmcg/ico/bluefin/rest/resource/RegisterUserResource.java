package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.User;

public class RegisterUserResource extends CommonUserResource implements Serializable {

	private static final long serialVersionUID = 304759584210372543L;

	@Size(min = 1, message = "Please provide role for the user")
	@NotNull(message = "Please provide a role for the user")
	private Set<Long> roles;
	
	@Size(min = 1, message = "Please provide legal entity for the user")
	@NotNull(message = "Please provide legal entity for the user")
	private Set<Long> legalEntityApps;
	
	@NotBlank(message = "Please provide time zone")
	private String timeZone;

	public User toUser(Collection<Role> roles, Collection<LegalEntityApp> legalEntityApps) {
		User user = new User();

		user.setUsername(this.getUsername());
		user.setFirstName(this.getFirstName());
		user.setLastName(this.getLastName());
		user.setEmail(this.getEmail());
		user.setSelectedTimeZone(timeZone);
		for (Role role : roles) {
			user.addRole(role);
		}
		for (LegalEntityApp legalEntityApp : legalEntityApps) {
			user.addLegalEntityApp(legalEntityApp);
		}

		return user;
	}

	public Set<Long> getRoles() {
		return roles;
	}

	public void setRoles(Set<Long> roles) {
		this.roles = roles;
	}

	public Set<Long> getLegalEntityApps() {
		return legalEntityApps;
	}

	public void setLegalEntityApps(Set<Long> legalEntityApps) {
		this.legalEntityApps = legalEntityApps;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	
}
