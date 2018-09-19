package com.mcmcg.ico.bluefin.service.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;

import com.mcmcg.ico.bluefin.repository.sql.Queries;

public class QueryBuilderHelper {
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
		return query.concat(" LIMIT "+offset +", "+pageSize);
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
		processLegalEntities(bf2,filterMap);
		processRoles(bf2,filterMap);
		processUserName(bf2,filterMap);
		processLastName(bf2,filterMap);
		processFirstName(bf2,filterMap);
		processEmail(bf2,filterMap);
		processStatus(bf2,filterMap);
		bf2.replace(0, 4, " ");
		return bf2.toString();
	}
	
	private static void processLegalEntities(StringBuilder bf2,Map<String,String> filterMap){
		if(isValidFilter(filterMap,"legalEntities")) {
			appendQuery(bf2," AND ule.LegalEntityAppID=:legalEntities");
		}
	}
	
	private static void processRoles(StringBuilder bf2,Map<String,String> filterMap){
		if(isValidFilter(filterMap,"roles")) {
			appendQuery(bf2," AND ur.RoleID=:roles ");
		}
	}
	
	private static void processUserName(StringBuilder bf2,Map<String,String> filterMap){
		if(isValidFilter(filterMap,"username")) {
			appendQuery(bf2," AND userName like :username");
		}	
	}
	
	private static void processLastName(StringBuilder bf2,Map<String,String> filterMap){
		if(isValidFilter(filterMap,"lastName")) {
			appendQuery(bf2," AND  lastName like :lastName ");
		}	
	}
	
	private static void processFirstName(StringBuilder bf2,Map<String,String> filterMap){
		if(isValidFilter(filterMap,"firstName")) {
			appendQuery(bf2," AND  firstName like :firstName ");
		}
	}
	
	private static void processEmail(StringBuilder bf2,Map<String,String> filterMap){
		if(isValidFilter(filterMap,"email")) {
			appendQuery(bf2," AND  email like :email ");
		}
	}
	
	/**private static void processStatus(StringBuilder bf2,Map<String,String> filterMap){
		if(isValidFilter(filterMap,"status")) {
			appendQuery(bf2," AND  status like :status ");
		}
	}*/
	
	private static void processStatus(StringBuilder bf2,Map<String,String> filterMap){
		if(isValidFilter(filterMap,"status")) {
		/**	appendQuery(bf2," AND status ="+filterMap.get("status"));*/
			appendQuery(bf2," AND status =:status ");
			
		}
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
