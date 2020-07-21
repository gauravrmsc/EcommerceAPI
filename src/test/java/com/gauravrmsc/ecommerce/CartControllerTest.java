package com.gauravrmsc.ecommerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gauravrmsc.ecommerce.model.persistence.Cart;
import com.gauravrmsc.ecommerce.model.persistence.Item;
import com.gauravrmsc.ecommerce.model.persistence.User;
import com.gauravrmsc.ecommerce.model.persistence.repositories.CartRepository;
import com.gauravrmsc.ecommerce.model.persistence.repositories.ItemRepository;
import com.gauravrmsc.ecommerce.model.persistence.repositories.UserRepository;
import com.gauravrmsc.ecommerce.model.requests.ModifyCartRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static com.gauravrmsc.ecommerce.security.SecurityConstants.HEADER_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {ECommerceApplication.class})
public class CartControllerTest {
  @Autowired
  MockMvc mockMvc;
  @MockBean
  ItemRepository itemRepository;
  @MockBean
  UserRepository userRepository;
  @MockBean
  CartRepository cartRepository;
  private static final String BASE_URL = "/api/cart";
  private static final String ADD_TO_CART_URL = BASE_URL + "/addToCart";
  private static final String REMOVE_FROM_CART_URL = BASE_URL + "/removeFromCart";
  private static final String authenticationToken =
      "BearereyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnYXVyYXYiLCJleHAiOjE1OTYxNjk0NDN9.xH82UbF3T_47uEYAo4m7QimX6CUo2XgnH3l7sSi5Olwdq_0zx9EtVrNKdIUfiOYbrlk7QU4dWlsdzg5CK46adw";
  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String USERNAME = "gaurav";
  private static final String PASSWORD = "987654321";
  private static final String ITEM_NAME = "Round Widget";
  private static final Long ITEM_ID = 1l;
  private static Item item;
  private static User user;
  private static Cart cart;

  @BeforeEach
  public void setup() {
    String hashedPassword = encoder.encode(PASSWORD);
    user = new User(USERNAME, hashedPassword);
    item = new Item(ITEM_ID, ITEM_NAME, new BigDecimal(2.99), "A widget that is round");
    cart = new Cart();
    cart.setId(1l);
    cart.setItems(new ArrayList<>());
    cart.setTotal(new BigDecimal(0));
    user.setCart(cart);
    cart.setUser(user);
  }

  @Test
  public void addToCartHappyPathTest() throws Exception {
    when(userRepository.findByUsername(USERNAME)).thenReturn(user);
    when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
    when(cartRepository.save(cart)).thenReturn(cart);
    ModifyCartRequest request = new ModifyCartRequest(USERNAME, ITEM_ID, 10);
    MockHttpServletResponse response = mockMvc.perform(
        post(ADD_TO_CART_URL).content(mapper.writeValueAsString(request))
            .contentType("application/json").header(HEADER_STRING, authenticationToken))
        .andExpect(status().isOk()).andReturn().getResponse();
    String responseText = response.getContentAsString();
    Cart responseCart = mapper.readValue(responseText, Cart.class);
    assertEquals(10, responseCart.getItems().size());
  }


  @Test
  public void addToCartErrorPath() throws Exception {
    //non registered user trying to add item to cart gets 404
    when(userRepository.findByUsername(USERNAME)).thenReturn(null);
    when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
    when(cartRepository.save(cart)).thenReturn(cart);
    ModifyCartRequest request = new ModifyCartRequest(USERNAME, ITEM_ID, 10);
    mockMvc.perform(post(ADD_TO_CART_URL).content(mapper.writeValueAsString(request))
        .contentType("application/json").header(HEADER_STRING, authenticationToken))
        .andExpect(status().isNotFound());

    // request for add to card for Item not present in inventory gets 404
    when(userRepository.findByUsername(USERNAME)).thenReturn(user);
    when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());
    when(cartRepository.save(cart)).thenReturn(cart);
    request = new ModifyCartRequest(USERNAME, ITEM_ID, 10);
    mockMvc.perform(post(ADD_TO_CART_URL).content(mapper.writeValueAsString(request))
        .contentType("application/json").header(HEADER_STRING, authenticationToken))
        .andExpect(status().isNotFound());
  }

  @Test
  public void removeFromCartHappyPathTest() throws Exception {
    when(userRepository.findByUsername(USERNAME)).thenReturn(user);
    when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
    when(cartRepository.save(cart)).thenReturn(cart);
    ModifyCartRequest request = new ModifyCartRequest(USERNAME, ITEM_ID, 10);
    MockHttpServletResponse response = mockMvc.perform(
        post(REMOVE_FROM_CART_URL).content(mapper.writeValueAsString(request))
            .contentType("application/json").header(HEADER_STRING, authenticationToken))
        .andExpect(status().isOk()).andReturn().getResponse();
    String responseText = response.getContentAsString();
    Cart responseCart = mapper.readValue(responseText, Cart.class);
    assertEquals(0, responseCart.getItems().size());
  }

  @Test
  public void removeFromCartErrorPath() throws Exception {
    //non registered user trying to remove item to cart gets 404
    when(userRepository.findByUsername(USERNAME)).thenReturn(null);
    when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
    when(cartRepository.save(cart)).thenReturn(cart);
    ModifyCartRequest request = new ModifyCartRequest(USERNAME, ITEM_ID, 10);
    mockMvc.perform(post(REMOVE_FROM_CART_URL).content(mapper.writeValueAsString(request))
        .contentType("application/json").header(HEADER_STRING, authenticationToken))
        .andExpect(status().isNotFound());

    // request for add to card for Item not present in inventory gets 404
    when(userRepository.findByUsername(USERNAME)).thenReturn(user);
    when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());
    when(cartRepository.save(cart)).thenReturn(cart);
    request = new ModifyCartRequest(USERNAME, ITEM_ID, 10);
    mockMvc.perform(post(REMOVE_FROM_CART_URL).content(mapper.writeValueAsString(request))
        .contentType("application/json").header(HEADER_STRING, authenticationToken))
        .andExpect(status().isNotFound());
  }

}
