package com.mcmcg.ico.bluefin.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provide password validation tools in a centralized location 
 * @author sa
 */
public class PasswordUtils {
	
	/**
	 * Default regular expression for password validation, if no regular expression is indicated on application settings this one will be used
	 */
	public static final String DEFAULT_REGULAR_EXPRESSION = "^(?=.*\\d)(?!.*[\\,])(?=.*[A-Z])(?=.*[a-z])(?=.*[\\!,\\^,\\#,\\$,\\%,\\*]).{10,}$";
	
	/**
	 * Private constructor to avoid instances creation as this is intended to be used as a static class
	 */
	private PasswordUtils() {
		
	}
	
	/**
	 * Return the regular expression for password strength validation
	 * @return if no value is defined in the settings this will return the default string else it will return what is set
	 */
	private static String getPasswordValidationRegularExpression() {
		return DEFAULT_REGULAR_EXPRESSION;
	}
	
	/**
	 * Validate the password strength according PCI requirements
	 * @param password: the password to be validated
	 * @return true if the password is strong enough false if not
	 */
	public static boolean validatePasswordStrength (String password) {
		// Create a Pattern object
		Pattern r = Pattern.compile(getPasswordValidationRegularExpression());
		// Now create matcher object.
		Matcher m = r.matcher(password);
		return m.find();
	}
	
	/**
	 * Checks if the username is in some part of the password
	 * @param userName
	 * @param password
	 * @return true if the username is written in some place into the password, false if not
	 */
	public static boolean containsUsername(final String userName,final String password) {
      return password.toLowerCase().indexOf(userName.toLowerCase())!=-1;
    }
    
}
