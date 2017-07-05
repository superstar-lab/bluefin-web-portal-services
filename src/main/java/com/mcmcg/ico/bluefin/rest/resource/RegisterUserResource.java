package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.User;

import lombok.Data;

@Data
public class RegisterUserResource implements Serializable {

	private static final long serialVersionUID = 304759584210372543L;

	@NotBlank(message = "Please provide user name")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field user name must be alphanumeric")
	private String username;

	@NotBlank(message = "Please provide first name for the user")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field first name must be alphanumeric")
	private String firstName;

	@NotBlank(message = "Please provide last name for the user")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field last name must be alphanumeric")
	private String lastName;

	@NotBlank(message = "Please provide email address for the user")
	private String email;

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

		user.setUsername(username);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
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
