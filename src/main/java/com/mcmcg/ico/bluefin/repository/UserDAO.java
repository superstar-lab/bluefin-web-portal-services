package com.mcmcg.ico.bluefin.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.mcmcg.ico.bluefin.model.User;
import com.mysema.query.types.expr.BooleanExpression;

public interface UserDAO {
	List<User> findAll();

	Page<User> findAll(BooleanExpression expression, PageRequest pageRequest);

	User findByUserId(long userId);

	User findByUsername(String username);

	User findByEmail(String email);

	long saveUser(User user);

	int updateUser(User user, String modifiedBy);

	int deleteByUsername(String username);
	
	
	Page<User> findAllUingFilter(BooleanExpression expression, PageRequest pageRequest);
	
}
