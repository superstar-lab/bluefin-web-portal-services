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

public class RegisterUserResource implements Serializable {

	private static final long serialVersionUID = 304759584210372543L;

	@NotBlank(message = "Please provide a user name")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field user name must be alphanumeric")
	private String username;

	@NotBlank(message = "Please provide a first name for the user")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field first name must be alphanumeric")
	private String firstName;

	@NotBlank(message = "Please provide a last name for the user")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field last name must be alphanumeric")
	private String lastName;

	@NotBlank(message = "Please provide an email address for the user")
	private String email;

	@Size(min = 1, message = "Please provide a role for the user")
	@NotNull(message = "Please provide a role for the user")
	private Set<Long> roles;

	@Size(min = 1, message = "Please provide a legal entity for the user")
	@NotNull(message = "Please provide a legal entity for the user")
	private Set<Long> legalEntityApps;
	
	@NotBlank(message = "Please provide time zone")
	private String timeZone;

	public User toUser(Collection<Role> roles, Collection<LegalEntityApp> legalEntityApps) {
		User user = new User();

		user.setUsername(username);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		for (Role role : roles) {
			user.addRole(role);
		}
		for (LegalEntityApp legalEntityApp : legalEntityApps) {
			user.addLegalEntityApp(legalEntityApp);
		}
		
		// Correct this when fixing code for User.
		// New User will not contain Role and Legal Entity App.
		// Create a new object to contain everything?
		// Roles
		// for (Role role : roles) {
		// user.addRole(role);
		// }
		// Legal Entity Apps
		// for (LegalEntityApp legalEntityApp : legalEntityApps) {
		// user.addLegalEntityApp(legalEntityApp);
		// }

		return user;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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
