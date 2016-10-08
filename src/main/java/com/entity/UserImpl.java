package com.entity;

import org.activiti.engine.identity.User;

public class UserImpl implements User{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6265590332339136379L;
	private String id;
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	
	public UserImpl() {
		super();
	}
	
	public UserImpl(String id, String firstName, String lastName, String email, String password) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
