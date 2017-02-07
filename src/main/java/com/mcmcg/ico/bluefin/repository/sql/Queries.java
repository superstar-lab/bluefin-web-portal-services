package com.mcmcg.ico.bluefin.repository.sql;

public interface Queries {
	String findAllApplications = "SELECT ApplicationID, ApplicationName FROM Application_Lookup";
}
