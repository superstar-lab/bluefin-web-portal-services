package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    public Property findByName(final String name);

}
