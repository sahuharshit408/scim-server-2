package com.example.scim.security;

import com.example.scim.config.SecurityConfig.ScimAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@order(1)
public class BearerTokenAuthFilter extends OncePerRequestFilter {
  private final String expectedToken;

  public BearerTokenAuthFilter(String expectedToken) {
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
    String token = auth.substring("Bearer ".length()).trim();
    if (!token.equals(expectedToken)) {
      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // Minimal: we don't populate SecurityContext here; Okta just needs 200s.
    // If you want to, you can set SecurityContextHolder with ScimAuthentication.
    chain.doFilter(req, res);
  }
}
