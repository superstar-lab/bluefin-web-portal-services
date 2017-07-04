package com.mcmcg.ico.bluefin.service.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import com.mcmcg.ico.bluefin.repository.sql.Queries;

public class QueryBuilderHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilderHelper.class);
	private QueryBuilderHelper(){
		// Default constructor
	}
	
	public static StringBuilder buildQuery(Map<String,String> filterMap ,Sort sort){
		StringBuilder  bf = new StringBuilder( Queries.FINDALLUSERS);
		setLegaEntity(bf,filterMap);
		setRoles(bf,filterMap);
		setWhere(bf,filterMap);
		bf.append(clauseAppender(filterMap));
		placeOderBy(bf,sort);
		return bf;
	}

	public static String appendLimit(String query,int offset,int pageSize){
		String queryVal = query.concat(" LIMIT "+offset +", "+pageSize);
		return queryVal;
	}
	
	 
	private static void setLegaEntity(StringBuilder  bf,Map<String,String> filterMap){
		 if(filterMap.containsKey("legalEntities"))
			 bf.append(" join User_LegalEntityApp  ule on ul.UserID=ule.UserID ");
	}
	
	private static void setRoles(StringBuilder  bf,Map<String,String> filterMap){
		 if(filterMap.containsKey("roles"))
			 bf.append(" join User_Role ur on ul.UserID=ur.UserID ");
	}
	
	private static void setWhere(StringBuilder  bf,Map<String,String> filterMap){
		if(filterMap.size()>0)
			bf.append(" Where ");
	}
	
	private static String clauseAppender(Map<String,String> filterMap){
		StringBuilder bf2 = new StringBuilder();
		if(isValidFilter(filterMap,"legalEntities"))
			appendQuery(bf2," AND ule.LegalEntityAppID=:legalEntities");
		if(isValidFilter(filterMap,"roles"))
			appendQuery(bf2," AND ur.RoleID=:roles ");
		if(isValidFilter(filterMap,"username"))
			appendQuery(bf2," AND userName like :username");		
		if(isValidFilter(filterMap,"lastName"))
			appendQuery(bf2," AND  lastName like :lastName ");
		if(isValidFilter(filterMap,"firstName"))
			appendQuery(bf2," AND  firstName like :firstName ");
		if(isValidFilter(filterMap,"email"))
			appendQuery(bf2," AND  email like :email ");
		if(isValidFilter(filterMap,"status"))
			appendQuery(bf2," AND  status like :status ");
		
		bf2.replace(0, 4, " ");
		return bf2.toString();
	}
	
	private static void appendQuery(StringBuilder bf2,String query){
		bf2.append(query);
	}
	private static boolean isValidFilter(Map<String,String> filterMap,String filterName){
		return filterMap.containsKey(filterName);
	}
	
	private static void placeOderBy(StringBuilder  bf,Sort sort){
		String sortString =  sort.toString();
			if(StringUtils.isNotEmpty(sortString)){
				String[] str1 = sortString.split(":");
				bf.append(" Order By ").append(str1[0]).append(" ").append(str1[1]);
			}
			
	}
}
