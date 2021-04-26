package com.mcmcg.ico.bluefin.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;
import com.mcmcg.ico.bluefin.security.model.SecurityUserFactory;

@Service
@Qualifier("customUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	@Autowired
	private UserDAO userDAO;
	@Autowired
	private SecurityUserFactory securityUserFactory;

	@Override
	public SecurityUser loadUserByUsername(String username) {
		LOGGER.info("UserDetailsServiceImpl -> loadUserByUsername, Entering to load User By Username {}", username);
		User user = this.userDAO.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
		} else {
			LOGGER.info("Exit from load User By Username : user found");
			return securityUserFactory.create(user);
		}
	}
}
