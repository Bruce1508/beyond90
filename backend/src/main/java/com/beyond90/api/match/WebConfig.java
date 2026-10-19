package com.beyond90.api.match;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Dev-time CORS allowlist for the Next.js frontend. `pnpm dev` falls back to
 * 3001+ if 3000 is already bound (observed locally: Docker's hbci listener
 * squats on 3000), so both are allowed here. Production origin must be added
 * explicitly (not wildcarded) once the Azure deployment URL is known (P0-20).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/v1/**")
				.allowedOrigins("http://localhost:3000", "http://localhost:3001")
				.allowedMethods("GET", "POST")
				.allowedHeaders("*");
	}
}
