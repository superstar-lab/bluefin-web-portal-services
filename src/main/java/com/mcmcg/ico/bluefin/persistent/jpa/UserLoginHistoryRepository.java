/**
 * 
 */
package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.UserLoginHistory;
 
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Long> { 
    
}
