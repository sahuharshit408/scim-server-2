package com.example.scim.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;


import java.io.IOException;

@Component
@Order(1)
public class BearerTokenAuthFilter extends OncePerRequestFilter {
  private final String expectedToken;

  public BearerTokenAuthFilter(@Value("${scim.token}") String expectedToken) {
    this.expectedToken = expectedToken;
    }

  @Override
  protected void doFilterInternal(HttpServletRequest req,
                                  HttpServletResponse res,
                                  FilterChain chain) throws ServletException, IOException {
    String auth = req.getHeader("Authorization");
    if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    System.out.println("🔐 BearerTokenAuthFilter:");
    System.out.println("Expected Token: " + expectedToken);

    String token = auth.substring("Bearer ".length()).trim();

    System.out.println("Received Token: " + token);

    if (!token.equals(expectedToken)) {


      System.out.println("Token not equal to expected Token !!!!!!!!!!!!!!!!!!!!!!!!!1");
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // Minimal: we don't populate SecurityContext here; Okta just needs 200s.
    // If you want to, you can set SecurityContextHolder with ScimAuthentication.
    chain.doFilter(req, res);
  }
}
