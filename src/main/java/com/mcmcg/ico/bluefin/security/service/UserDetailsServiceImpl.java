package com.mcmcg.ico.bluefin.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.repository.RoleDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.repository.UserRoleDAO;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;
import com.mcmcg.ico.bluefin.security.model.SecurityUserFactory;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	@Autowired
	private UserDAO userDAO;

	@Override
	public SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException {
		LOGGER.info("Entering UserDetailsServiceImpl :: loadUserByUsername()");
		User user = this.userDAO.findByUsername(username);
		LOGGER.debug("Entering UserDetailsServiceImpl :: loadUserByUsername() : user is : "+user);
		if (user == null) {
			throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
		} else {
			LOGGER.info("Exit UserDetailsServiceImpl :: loadUserByUsername() : user found");
			return SecurityUserFactory.create(user);
		}
	}
}
