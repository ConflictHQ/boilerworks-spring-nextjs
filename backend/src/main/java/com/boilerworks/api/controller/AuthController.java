package com.boilerworks.api.controller;

import com.boilerworks.api.dto.ApiResponse;
import com.boilerworks.api.dto.LoginRequest;
import com.boilerworks.api.dto.UserResponse;
import com.boilerworks.api.model.AppUser;
import com.boilerworks.api.repository.AppUserRepository;
import com.boilerworks.api.security.BoilerworksUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final AppUserRepository userRepository;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<UserResponse>> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    try {
      Authentication auth =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(auth);
      SecurityContextHolder.setContext(context);

      HttpSession session = httpRequest.getSession(true);
      session.setAttribute(
          HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

      BoilerworksUserDetails userDetails = (BoilerworksUserDetails) auth.getPrincipal();
      AppUser user = userRepository.findById(userDetails.getUserId()).orElseThrow();
      user.setLastLogin(Instant.now());
      userRepository.save(user);

      return ResponseEntity.ok(ApiResponse.ok(new UserResponse(user)));
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(401).body(ApiResponse.error("Invalid email or password"));
    }
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> me(
      @AuthenticationPrincipal BoilerworksUserDetails userDetails) {
    if (userDetails == null) {
      return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
    }
    AppUser user = userRepository.findById(userDetails.getUserId()).orElseThrow();
    return ResponseEntity.ok(ApiResponse.ok(new UserResponse(user)));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
    request.getSession().invalidate();
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok(ApiResponse.ok());
  }
}
