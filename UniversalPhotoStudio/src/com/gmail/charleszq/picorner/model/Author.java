/**
 * 
 */
package com.gmail.charleszq.picorner.model;

import java.io.Serializable;

/**
 * @author charles(charleszq@gmail.com)
 *
 */
public class Author implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1250227853225988732L;
	
	private String userId;
	private String userName;
	private String buddyIconUrl;
	
	public String getBuddyIconUrl() {
		return buddyIconUrl;
	}
	public void setBuddyIconUrl(String buddyIconUrl) {
		this.buddyIconUrl = buddyIconUrl;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName == null ? userId : userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	

}
