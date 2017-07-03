package com.mcmcg.ico.bluefin.rest.controller.exception;

public class ApplicationGenericException extends Exception {
	private static final long serialVersionUID = 2009201171724648178L;

	public ApplicationGenericException() {
	}

	public ApplicationGenericException(String message) {
		super(message);
	}

	public ApplicationGenericException(Throwable cause) {
		super(cause);
	}

	public ApplicationGenericException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApplicationGenericException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
