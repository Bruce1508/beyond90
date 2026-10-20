package com.beyond90.api.match;

import com.beyond90.api.simulation.InvalidActionException;
import com.beyond90.api.xray.MomentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps domain exceptions to contracts/openapi.yaml's application/problem+json Problem schema. */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MatchNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleMatchNotFound(MatchNotFoundException exception) {
		return notFound(exception.getMessage());
	}

	@ExceptionHandler(MomentNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleMomentNotFound(MomentNotFoundException exception) {
		return notFound(exception.getMessage());
	}

	@ExceptionHandler(InvalidActionException.class)
	public ResponseEntity<ProblemDetail> handleInvalidAction(InvalidActionException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
		problem.setTitle("Invalid intervention or domain constraint");
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.body(problem);
	}

	private static ResponseEntity<ProblemDetail> notFound(String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail);
		problem.setTitle("Resource absent");
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.body(problem);
	}
}
