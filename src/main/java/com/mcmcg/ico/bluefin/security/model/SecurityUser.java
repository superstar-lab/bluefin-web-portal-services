package com.mcmcg.ico.bluefin.security.model;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mcmcg.ico.bluefin.model.User;

@JsonIgnoreProperties({ "password", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled",
		"email", "authorities" })
public class SecurityUser implements UserDetails {

	private static final long serialVersionUID = 1680188661109204234L;
	private User user;
	private Collection<? extends GrantedAuthority> authorities;
	private Boolean accountNonExpired = true;
    private Boolean accountNonLocked = false;
	private Boolean credentialsNonExpired = true;
	private Boolean enabled = false;
	private Date expires;

	public SecurityUser() {
		super();
	}

	public SecurityUser(User user, Collection<? extends GrantedAuthority> authorities) {
		this.user = user;
		this.authorities = authorities;
	}

	@Override
	public String getPassword() {
		return user == null ? null : user.getPassword();
	}

	@Override
	public String getUsername() {
		return user == null ? null : user.getUsername();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.getAccountNonExpired();
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return !("INACTIVE".equals(user.getStatus()) || "LOCKED".equals(user.getStatus()));
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.getCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return "ACTIVE".equals(user.getStatus());
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean getAccountNonExpired() {
		return accountNonExpired;
	}

	public void setAccountNonExpired(Boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public Boolean getAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(Boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public Boolean getCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
	}
	
	
	
}
