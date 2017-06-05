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
	
	public AuthenticationResponse() {
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

	/**
	 * @return the selectedTimeZone
	 */
	public String getSelectedTimeZone() {
		return selectedTimeZone;
	}

	/**
	 * @param selectedTimeZone the selectedTimeZone to set
	 */
	public void setSelectedTimeZone(String selectedTimeZone) {
		this.selectedTimeZone = selectedTimeZone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}