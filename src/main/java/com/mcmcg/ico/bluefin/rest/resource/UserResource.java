package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.model.UserRole;

public class UserResource implements Serializable {

	private static final long serialVersionUID = 2895903899201191359L;

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
	private Set<Role> roles;

	@Size(min = 1, message = "Please provide a legal entity for the user")
	@NotNull(message = "Please provide a legal entity for the user")
	private Set<LegalEntityApp> legalEntityApps;
	
	private String selectedTimeZone;

	private String status;

	public UserResource() {
	}

	public UserResource(User user) {
		this.username = user.getUsername();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.status = user.getStatus();
		this.email = user.getEmail();
		this.selectedTimeZone = user.getSelectedTimeZone();
		roles = new HashSet<Role>();
		if(user.getRoles() != null) {
			for (UserRole role : user.getRoles()) {
				Role roleObj = new Role();
				roleObj.setRoleId(role.getRoleId());
				this.roles.add(roleObj);
			}
		}
		legalEntityApps = new HashSet<LegalEntityApp>();
		if (user.getLegalEntities() != null) {
			for (UserLegalEntityApp legalEntity : user.getLegalEntities()) {
				LegalEntityApp userLegal = new LegalEntityApp();
				userLegal.setLegalEntityAppId(legalEntity.getLegalEntityAppId());
				this.legalEntityApps.add(userLegal);
			}
		}
		/*for (UserLegalEntityApp legalEntityApp : user.getLegalEntities()) {
			this.legalEntityApps.add(legalEntityApp.get);
		}*/
		// Correct this when fixing code for User.
		// New User will not contain Role and Legal Entity App.
		// Create a new object to contain everything?
		// this.legalEntityApps = new
		// HashSet<LegalEntityApp>(user.getLegalEntityApps());
		// this.roles = new HashSet<Role>(user.getRoleNames());
	}

	public User toUser(Set<UserRole> roles, Set<UserLegalEntityApp> entities) {
		User user = new User();

		user.setUsername(username);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.setStatus(status);
		user.setRoles(roles);
		user.setLegalEntities(entities);

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

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Set<LegalEntityApp> getLegalEntityApps() {
		return legalEntityApps;
	}

	public void setLegalEntityApps(Set<LegalEntityApp> legalEntityApps) {
		this.legalEntityApps = legalEntityApps;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSelectedTimeZone() {
		return selectedTimeZone;
	}

	public void setSelectedTimeZone(String selectedTimeZone) {
		this.selectedTimeZone = selectedTimeZone;
	}
}