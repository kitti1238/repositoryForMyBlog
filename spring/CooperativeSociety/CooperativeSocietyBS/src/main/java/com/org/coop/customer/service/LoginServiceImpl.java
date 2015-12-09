package com.org.coop.customer.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.org.coop.society.data.customer.entities.User;
import com.org.coop.society.data.customer.entities.UserRole;
import com.org.coop.society.data.customer.repositories.UserRepository;

@Service
public class LoginServiceImpl implements LoginService {
	private static final Logger log = Logger.getLogger(LoginServiceImpl.class); 

	@Autowired
	private UserRepository userRepository;
	
	/**
	 * This method returns list of roles for a given user name
	 */
	public List<String> getRole(String username) {
		List<String> roleList = new ArrayList<String>();
		User user = userRepository.findByUserName(username);
		if(user != null) {
			for(UserRole userRole : user.getUserRoles()) {
				roleList.add(userRole.getRoleMaster().getRoleName().toUpperCase());
			}
		}
		return roleList;
	}
	
	/**
	 * This method returns list of permissions for a given user name
	 */
	public List<String> getRolePermissions(String username) {
		List<String> roleList = new ArrayList<String>();
		User user = userRepository.findByUserName(username);
		if(user != null) {
			for(UserRole userRole : user.getUserRoles()) {
				roleList.add(userRole.getRoleMaster().getRoleName().toUpperCase());
			}
		}
		return roleList;
	}
}
