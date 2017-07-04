package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.Set;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.User;

import lombok.Data;

@Data
public class UpdateUserResource implements Serializable {

	private static final long serialVersionUID = 2025146972481454967L;

	@NotBlank(message = "Please provide a first name for the user")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field first name must be alphanumeric")
	private String firstName;

	@NotBlank(message = "Please provide a last name for the user")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field last name must be alphanumeric")
	private String lastName;

	@NotBlank(message = "Please provide an email address for the user")
	private String email;
	
	@NotBlank(message = "Please provide time zone")
	private String selectedTimeZone;

	public UserResource toUserResource(Set<Role> roles, Set<LegalEntityApp> entities) {
		UserResource userResource = new UserResource();
		userResource.setFirstName(firstName);
		userResource.setLastName(lastName);
		userResource.setEmail(email);
		// Correct this when fixing code for User.
		// New User will not contain Role and Legal Entity App.
		// Create a new object to contain everything?
		userResource.setRoles(roles);
		userResource.setLegalEntityApps(entities);
		userResource.setSelectedTimeZone(selectedTimeZone);
		return userResource;
	}

	public User updateUser(User user) {
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		return user;
	}
	
}
