package com.beyond90.api.match;

/** Thrown when a requested matchId has no loaded fixture. Mapped to HTTP 404 by {@link ApiExceptionHandler}. */
public class MatchNotFoundException extends RuntimeException {

	public MatchNotFoundException(String matchId) {
		super("Match not found: " + matchId);
	}
}
