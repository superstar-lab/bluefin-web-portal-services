package com.mcmcg.ico.bluefin.security.rest.resource;

import java.io.Serializable;
import java.util.Set;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Permission;
import com.mcmcg.ico.bluefin.model.Role;

import lombok.Data;
@Data
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
}