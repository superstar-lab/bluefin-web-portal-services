package com.mcmcg.ico.bluefin.repository;

import java.util.List;
import java.util.Map;

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
	
<<<<<<< HEAD
	public Page<User> findAllWithDynamicFilter(List<String> search, PageRequest pageRequest,Map<String,String> filterMap );
=======
	
	Page<User> findAllUingFilter(BooleanExpression expression, PageRequest pageRequest);
	
>>>>>>> branch 'ORM_Refactoring' of http://tfs-prd.internal.mcmcg.com:8080/tfs/Encore/ICO/_git/Bluefin-web-portal-services
}
