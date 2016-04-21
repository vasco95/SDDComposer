package com.sddc.models.datacollectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="users")
public class Users {
    @Id
    private String id;
    private String userName;
    private String passWord;
    public Users() {}
    public Users(String username, String password) {
        this.userName = username;
        this.passWord = password;
    }
	 /**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param username the userName to set
	 */
	public void setUserName(String username) {
		this.userName = username;
	}
	/**
	 * @return the passWord
	 */
	public String getPassWord() {
		return passWord;
	}
	/**
	 * @param password the passWord to set
	 */
	public void setPassWord(String password) {
		this.passWord = password;
	}
}
