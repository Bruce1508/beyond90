package com.beyond90.api.simulation;

/**
 * Thrown by {@link BranchSimulator} when a requested action fails validation (actor does not
 * possess the ball, missing/self-targeted PASS receiver, out-of-bounds destination, or a SHOOT
 * target not on the attacking goal line). Mapped to HTTP 422 by the branch API (P0-14).
 */
public class InvalidActionException extends RuntimeException {

	public InvalidActionException(String message) {
		super(message);
	}
}
