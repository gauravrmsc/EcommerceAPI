package com.gauravrmsc.ecommerce.controllers;

import com.gauravrmsc.ecommerce.model.persistence.Cart;
import com.gauravrmsc.ecommerce.model.persistence.Item;
import com.gauravrmsc.ecommerce.model.persistence.User;
import com.gauravrmsc.ecommerce.model.persistence.repositories.CartRepository;
import com.gauravrmsc.ecommerce.model.persistence.repositories.ItemRepository;
import com.gauravrmsc.ecommerce.model.persistence.repositories.UserRepository;
import com.gauravrmsc.ecommerce.model.requests.ModifyCartRequest;
import java.util.Optional;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
  private static final Logger logger = LoggerFactory.getLogger(CartController.class);
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CartRepository cartRepository;

  @Autowired
  private ItemRepository itemRepository;

  @PostMapping("/addToCart")
  public ResponseEntity<Cart> addTocart(@RequestBody ModifyCartRequest request) throws Exception {
    logger.info("User {} added {} to cart", request.getUsername(), request.getItemId());
    User user = userRepository.findByUsername(request.getUsername());
    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    Optional<Item> item = itemRepository.findById(request.getItemId());
    if (!item.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    Cart cart = user.getCart();
    IntStream.range(0, request.getQuantity()).forEach(i -> cart.addItem(item.get()));
    cartRepository.save(cart);
    return ResponseEntity.ok(cart);
  }

  @PostMapping("/removeFromCart")
  public ResponseEntity<Cart> removeFromcart(@RequestBody ModifyCartRequest request) {
    logger.info("User {} removed {} from cart", request.getUsername(), request.getItemId());
    User user = userRepository.findByUsername(request.getUsername());
    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    Optional<Item> item = itemRepository.findById(request.getItemId());
    if (!item.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    Cart cart = user.getCart();
    IntStream.range(0, request.getQuantity()).forEach(i -> cart.removeItem(item.get()));
    cartRepository.save(cart);
    return ResponseEntity.ok(cart);
  }

}
