package com.org.coop.admin.service;

import java.util.List;

public interface AdminLoginService {
	/**
	 * This method returns list of roles for a given user name
	 */
	public List<String> getRole(String username);
	
	/**
	 * This method returns list of permissions for a given user name
	 */
	public List<String> getRolePermissions(String username);
}
