package com.boilerworks.api.config;

import com.boilerworks.api.security.BoilerworksUserDetails;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class JpaAuditingConfig {

  @Bean
  public AuditorAware<UUID> auditorAware() {
    return () -> {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
        return Optional.empty();
      }
      if (auth.getPrincipal() instanceof BoilerworksUserDetails userDetails) {
        return Optional.of(userDetails.getUserId());
      }
      return Optional.empty();
    };
  }
}
