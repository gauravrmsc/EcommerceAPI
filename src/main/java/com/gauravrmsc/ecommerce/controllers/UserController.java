package com.gauravrmsc.ecommerce.controllers;

import com.gauravrmsc.ecommerce.model.persistence.Cart;
import com.gauravrmsc.ecommerce.model.persistence.User;
import com.gauravrmsc.ecommerce.model.persistence.repositories.CartRepository;
import com.gauravrmsc.ecommerce.model.persistence.repositories.UserRepository;
import com.gauravrmsc.ecommerce.model.requests.CreateUserRequest;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
  Logger log = LogManager.getLogger(this.getClass().getName());
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CartRepository cartRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @GetMapping("/id/{id}")
  public ResponseEntity<User> findById(@PathVariable Long id) {
    Optional<User> user = userRepository.findById(id);
    return ResponseEntity.of(user);
  }

  @GetMapping("/{username}")
  public ResponseEntity<User> findByUserName(@PathVariable String username) {
    User user = userRepository.findByUsername(username);
    return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
  }

  @PostMapping("/create")
  public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
    if (createUserRequest == null || createUserRequest.getUsername() == null
        || createUserRequest.getPassword() == null
        || createUserRequest.getConfirmPassword() == null) {
      log.error("Invalid User Details Provided");
      ResponseEntity<User> errorResponse =
          new ResponseEntity("Invalid user details  provided", HttpStatus.BAD_REQUEST);
      return errorResponse;
    }
    log.info("Creating User {}", createUserRequest.getUsername());
    if (createUserRequest.getPassword().length() < 7 || !createUserRequest.getPassword()
        .equals(createUserRequest.getConfirmPassword())) {
      log.error("Create User Request for {} failed", createUserRequest.getUsername());
      ResponseEntity<User> response =
          new ResponseEntity("User Details does not meet the specified constraints",
              HttpStatus.BAD_REQUEST);
      return response;
    }

    if (userRepository.findByUsername(createUserRequest.getUsername()) != null) {
      ResponseEntity<User> response =
          new ResponseEntity("User Already Exist", HttpStatus.BAD_REQUEST);
      return response;
    }
    User user = new User();
    user.setUsername(createUserRequest.getUsername());
    Cart cart = new Cart();
    cartRepository.save(cart);
    user.setCart(cart);
    user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
    userRepository.save(user);
    log.info("User Account for {} created Successfully", createUserRequest.getUsername());
    return ResponseEntity.ok(user);
  }

}
