package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.User;

import lombok.Data;

@Data
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

}
