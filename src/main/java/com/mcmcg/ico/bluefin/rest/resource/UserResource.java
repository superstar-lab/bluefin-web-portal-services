package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.model.UserRole;

public class UserResource extends CommonUserResource implements Serializable {

	private static final long serialVersionUID = 2895903899201191359L;
	
	@Size(min = 1, message = "Please provide a role for the user")
	@NotNull(message = "Please provide a role for the user")
	private Set<Role> roles;
	
	@Size(min = 1, message = "Please provide legal entity for the user")
	@NotNull(message = "Please provide legal entity for the user")
	private Set<LegalEntityApp> legalEntityApps;
	
	private String selectedTimeZone;

	private String status;

	public UserResource() {
		roles = new HashSet<>();
		legalEntityApps = new HashSet<>();
	}

	public UserResource(User user) {
		this.setUsername(user.getUsername());
		this.setFirstName(user.getFirstName());
		this.setLastName(user.getLastName());
		this.setStatus(user.getStatus());
		this.setEmail(user.getEmail());
		this.setSelectedTimeZone(user.getSelectedTimeZone());
		roles = new HashSet<>();
		if(user.getRoles() != null) {
			for (UserRole role : user.getRoles()) {
				Role roleObj = new Role();
				roleObj.setRoleId(role.getRoleId());
				this.getRoles().add(roleObj);
			}
		}
		legalEntityApps = new HashSet<>();
		if (user.getLegalEntities() != null) {
			for (UserLegalEntityApp legalEntity : user.getLegalEntities()) {
				LegalEntityApp userLegal = new LegalEntityApp();
				userLegal.setLegalEntityAppId(legalEntity.getLegalEntityAppId());
				this.getLegalEntityApps().add(userLegal);
			}
		}
	}

	public User toUser(Set<UserRole> roles, Set<UserLegalEntityApp> entities) {
		User user = new User();

		user.setUsername(this.getUsername());
		user.setFirstName(this.getFirstName());
		user.setLastName(this.getLastName());
		user.setEmail(this.getEmail());
		user.setStatus(this.getStatus());
		user.setRoles(roles);
		user.setLegalEntities(entities);

		return user;
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

	public String getSelectedTimeZone() {
		return selectedTimeZone;
	}

	public void setSelectedTimeZone(String selectedTimeZone) {
		this.selectedTimeZone = selectedTimeZone;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
}