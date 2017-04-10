package com.mcmcg.ico.bluefin.service.util;

import java.util.Map;

import com.mcmcg.ico.bluefin.repository.sql.Queries;

public class QueryBuilderHelper {
	

	public static String buildQuery(Map<String,String> filterMap ){
		//String query = Queries.findAllUsers;
		StringBuffer  bf = new StringBuffer( Queries.findAllUsers);
		
		setLegaEntity(bf,filterMap);
		setRoles(bf,filterMap);
		setWhere(bf,filterMap);
		bf.append(clauseAppender(filterMap));
		//bf.append("LIMIT :offset, :pageSize");
		
		return bf.toString();
	}

	public static String appendLimit(String query,int offset,int pageSize){
		
		query = query.concat(" LIMIT "+offset +", "+pageSize);
		
		return query;
	}
	
	 
	private static void setLegaEntity(StringBuffer  bf,Map<String,String> filterMap){
		 if(filterMap.containsKey("legalEntities"))
			 bf.append(" join User_LegalEntityApp  ule on ul.UserID=ule.UserID ");
	}
	private static void setRoles(StringBuffer  bf,Map<String,String> filterMap){
		 if(filterMap.containsKey("roles"))
			 bf.append(" join User_Role ur on ul.UserID=ur.UserID ");
	}
	private static void setWhere(StringBuffer  bf,Map<String,String> filterMap){
		if(filterMap.size()>0)
			bf.append(" Where ");
	}
	
	private static String clauseAppender(Map<String,String> filterMap){
		StringBuffer bf2 = new StringBuffer();
		if(filterMap.containsKey("legalEntities"))
			bf2.append(" AND ule.LegalEntityAppID=:legalEntities");
		if(filterMap.containsKey("roles"))
			bf2.append(" AND ur.roles=:roles ");
		if(filterMap.containsKey("username"))
			bf2.append(" AND userName=:username ");		
		if(filterMap.containsKey("lastName"))
			bf2.append(" AND  lastName:lastName ");
		if(filterMap.containsKey("firstName"))
			bf2.append(" AND  firstName=:firstName ");
		if(filterMap.containsKey("email"))
			bf2.append(" AND  emailId=:email ");
		if(filterMap.containsKey("status"))
			bf2.append(" AND  status:status ");
		
		bf2.replace(0, 4, " ");
		return bf2.toString();
	}
}
