package com.beyond90.api.xray;

/** Thrown when a requested momentId has no loaded moment. Mapped to HTTP 404 by {@link com.beyond90.api.match.ApiExceptionHandler}. */
public class MomentNotFoundException extends RuntimeException {

	public MomentNotFoundException(String momentId) {
		super("Moment not found: " + momentId);
	}
}
