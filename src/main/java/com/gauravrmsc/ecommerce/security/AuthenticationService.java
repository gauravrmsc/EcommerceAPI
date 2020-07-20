package com.gauravrmsc.ecommerce.security;

import com.gauravrmsc.ecommerce.model.persistence.User;
import com.gauravrmsc.ecommerce.model.persistence.repositories.UserRepository;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements AuthenticationProvider {
  private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
  @Autowired
  UserRepository userRepository;
  @Autowired
  BCryptPasswordEncoder passwordEncoder;
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String userName = authentication.getName();
    String password = authentication.getCredentials().toString();
    User user = userRepository.findByUsername(userName);
    if (user != null && passwordEncoder.matches(password,user.getPassword())) {
      logger.info("User {} logged into his account",userName);
      return new UsernamePasswordAuthenticationToken(userName, password, new ArrayList<>());
    }
    logger.warn("Invaid Login Attempt id={}",userName);
    return null;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return  authentication.equals(UsernamePasswordAuthenticationToken.class) ;
  }
}
