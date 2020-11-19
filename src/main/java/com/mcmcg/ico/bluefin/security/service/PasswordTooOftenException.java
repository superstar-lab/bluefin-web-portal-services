package com.mcmcg.ico.bluefin.security.service;

import org.springframework.security.core.AuthenticationException;

public class PasswordTooOftenException extends AuthenticationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PasswordTooOftenException(String msg) {
		super(msg);
	}

	/**
	 * Constructs a {@code UsernameNotFoundException} with the specified message and root
	 * cause.
	 *
	 * @param msg the detail message.
	 * @param t root cause
	 */
	public PasswordTooOftenException(String msg, Throwable t) {
		super(msg, t);
	}
}
