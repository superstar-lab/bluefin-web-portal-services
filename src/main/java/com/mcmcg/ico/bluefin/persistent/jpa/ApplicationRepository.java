package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.persistent.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

}
