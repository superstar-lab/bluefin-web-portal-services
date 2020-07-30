package com.mcmcg.ico.bluefin.rest.resource;

public class UpdatePasswordResource {
    private String oldPassword;
    private String newPassword;
    
	public String getOldPassword() {
		return oldPassword;
	}
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}   
}