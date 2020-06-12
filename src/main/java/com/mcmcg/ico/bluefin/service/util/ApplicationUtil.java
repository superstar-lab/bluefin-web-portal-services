package com.mcmcg.ico.bluefin.service.util;


public class ApplicationUtil {
	private ApplicationUtil(){
		// Default Constructor 
	}
	/**
	 * Get the value of parameter in search string.
	 * 
	 * @param search
	 *            string
	 * @param parameter
	 *            in search string
	 * 
	 * @return value of parameter
	 */
	public static String getValueFromParameter(String search, String parameter) {

		String value = null;
		String[] array1 = search.split("\\$\\$");

		for (String pair : array1) {
			if (pair.startsWith(parameter)) {
				String[] array2 = pair.split(":");
				value = array2[1];
				break;
			}
		}

		return value;
	}
}
