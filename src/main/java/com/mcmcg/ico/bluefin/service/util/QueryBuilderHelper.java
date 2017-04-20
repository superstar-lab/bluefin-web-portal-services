package com.mcmcg.ico.bluefin.service.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;

import com.mcmcg.ico.bluefin.repository.sql.Queries;

public class QueryBuilderHelper {
	

	public static StringBuffer buildQuery(Map<String,String> filterMap ,Sort sort){
		//String query = Queries.findAllUsers;
		StringBuffer  bf = new StringBuffer( Queries.findAllUsers);
		
		setLegaEntity(bf,filterMap);
		setRoles(bf,filterMap);
		setWhere(bf,filterMap);
		bf.append(clauseAppender(filterMap));
		placeOderBy(bf,sort);
		
		return bf;
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
			bf2.append(" AND ur.RoleID=:roles ");
		if(filterMap.containsKey("username"))
			bf2.append(" AND userName like :username");		
		if(filterMap.containsKey("lastName"))
			bf2.append(" AND  lastName like :lastName ");
		if(filterMap.containsKey("firstName"))
			bf2.append(" AND  firstName like :firstName ");
		if(filterMap.containsKey("email"))
			bf2.append(" AND  email like :email ");
		if(filterMap.containsKey("status"))
			bf2.append(" AND  status like :status ");
		
		bf2.replace(0, 4, " ");
		return bf2.toString();
	}
	
	private static void placeOderBy(StringBuffer  bf,Sort sort){
		String sortString =  sort.toString();
			if(StringUtils.isNotEmpty(sortString)){
				String[] str1 = sortString.split(":");
				bf.append(" Order By ").append(str1[0]).append(" ").append(str1[1]);
			}
			
	}
}
