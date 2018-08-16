/**
 * 
 */
package com.mcmcg.ico.bluefin.service.util;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;

/**
 * @author sarora13
 *
 */
public class LoggingUtil {
	
	/**
	 * 
	 */
	private LoggingUtil() {
		
	}
	
	/**
	 * 
	 * @param args
	 * @return
	 */
	public static String adminAuditInfo(String...args) {
		StringBuilder strBuilder = new StringBuilder(BluefinWebPortalConstants.ADMINAUDITINFO)
										.append(BluefinWebPortalConstants.SEPARATOR);
		return arrayToString(strBuilder, args);
	}
	
	/**
	 * 
	 * @param args
	 * @return
	 */
	public static String invalidLoginAttempts(String...args) {
		StringBuilder strBuilder = new StringBuilder(BluefinWebPortalConstants.INVALIDLOGINATTEMPT)
										.append(BluefinWebPortalConstants.SEPARATOR);
		return arrayToString(strBuilder, args);
	}
	
	/**
	 * 
	 * @param strBuilder
	 * @param args
	 * @return
	 */
	private static String arrayToString(StringBuilder strBuilder, String...args) {
		for(String arg : args) {
			strBuilder.append(arg);
		}
		return strBuilder.toString();
	}

}
