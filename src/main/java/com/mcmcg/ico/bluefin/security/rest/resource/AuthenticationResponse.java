package com.mcmcg.ico.bluefin.security.rest.resource;

import java.io.Serializable;
import java.util.Set;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Permission;
import com.mcmcg.ico.bluefin.model.Role;

public class AuthenticationResponse implements Serializable {

	private static final long serialVersionUID = 5561968317615904891L;

	private String token;
	private String username;
	private String firstName;
	private String lastName;
	private Set<Role> roles;
	private Set<Permission> permissions;
	private Set<LegalEntityApp> legalEntityApps;
	private String selectedTimeZone;
	private String email;
	private String warn;
	/*UI not take any action if value '-1' else if value = 0 then redirect to password change screen else show warning message after successful login.*/
	private int changePasswordWithIn = -1;
	
	public AuthenticationResponse() {
		// Default Constructor
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWarn() {
		return warn;
	}

	public void setWarn(String warn) {
		this.warn = warn;
	}

	public int getChangePasswordWithIn() {
		return changePasswordWithIn;
	}

	public void setChangePasswordWithIn(int changePasswordWithIn) {
		this.changePasswordWithIn = changePasswordWithIn;
	}
	
	
}