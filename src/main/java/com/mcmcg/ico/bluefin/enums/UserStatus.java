/**
 * 
 */
package com.mcmcg.ico.bluefin.enums;

/**
 * @author sarora13
 *
 */
public enum UserStatus {
	
	NEW(1,"NEW"),
	ACTIVE(2,"ACTIVE"),
	INACTIVE(3, "INACTIVE"),
	LOCKED(4, "LOCKED");
	
	private int statusId;
	private String status;
	
	public int getStatusId() {
		return statusId;
	}

	public String getStatus() {
		return status;
	}

	private UserStatus(int statusId, String status) {
		this.statusId = statusId;
		this.status = status;
	}
	
	/**
	 * 
	 * @param status
	 * @return
	 */
	public UserStatus getEnumByStatus(String status) {
		for(UserStatus userStatus : UserStatus.values()) {
			if(userStatus.getStatus().equalsIgnoreCase(status))
				return userStatus;
		}
		return null;
	}
	
	/**
	 * 
	 * @param statusId
	 * @return
	 */
	public UserStatus getEnumByStatusId(int statusId) {
		for(UserStatus userStatus : UserStatus.values()) {
			if(userStatus.getStatusId() == (statusId))
				return userStatus;
		}
		return null;
	}
}
