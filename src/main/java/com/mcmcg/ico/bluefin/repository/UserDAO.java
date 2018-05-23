package com.mcmcg.ico.bluefin.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserPasswordHistory;
import com.mysema.query.types.expr.BooleanExpression;

public interface UserDAO {
	List<User> findAll();
	
	Page<User> findAll(BooleanExpression expression, PageRequest pageRequest);
	
	User findByUserId(long userId);

	User findByUsername(String username);

	long saveUser(User user);

	int updateUser(User user, String modifiedBy);
	
	public Page<User> findAllWithDynamicFilter(List<String> search, PageRequest pageRequest,Map<String,String> filterMap );

	int updateUserLastLogin(User user);
	
	ArrayList<UserPasswordHistory> getPasswordHistoryById(long userId);
	
	long savePasswordHistory(User user, String modifiedBy, String userPreviousPasword);
	
	void updatePasswordHistory(long historyId, String modifiedBy, String previousPassword);

}
