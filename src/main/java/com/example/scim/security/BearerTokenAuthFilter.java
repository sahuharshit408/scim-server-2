package com.example.scim.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class BearerTokenAuthFilter extends OncePerRequestFilter {

  private final String expectedToken;

  public BearerTokenAuthFilter(@Value("${scim.token}") String expectedToken) {
    this.expectedToken = expectedToken;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");

    if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
      System.out.println("🚫 Missing or invalid Authorization header");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String token = authHeader.substring("Bearer ".length()).trim();

    if (!token.equals(expectedToken)) {
      System.out.printf("🚫 Invalid token. Expected: '%s', Received: '%s'%n", expectedToken, token);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // ✅ Token is valid — allow request to proceed
    System.out.println("✅ Auth passed: token accepted");
    filterChain.doFilter(request, response);
  }
}
