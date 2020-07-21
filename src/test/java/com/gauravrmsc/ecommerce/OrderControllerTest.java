package com.gauravrmsc.ecommerce;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gauravrmsc.ecommerce.model.persistence.Cart;
import com.gauravrmsc.ecommerce.model.persistence.Item;
import com.gauravrmsc.ecommerce.model.persistence.User;
import com.gauravrmsc.ecommerce.model.persistence.UserOrder;
import com.gauravrmsc.ecommerce.model.persistence.repositories.OrderRepository;
import com.gauravrmsc.ecommerce.model.persistence.repositories.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {ECommerceApplication.class})
public class OrderControllerTest {
  @Autowired
  MockMvc mockMvc;
  @MockBean
  UserRepository userRepository;
  @MockBean
  OrderRepository orderRepository;
  private static final String BASE_URL = "/api/order";
  private static final String PLACE_ORDER_URL = BASE_URL + "/submit";
  private static final String HISTORY_URL = BASE_URL + "/history";
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
  private static UserOrder order;

  @BeforeEach
  public void setup() {
    String hashedPassword = encoder.encode(PASSWORD);
    user = new User(USERNAME, hashedPassword);
    item = new Item(ITEM_ID, ITEM_NAME, new BigDecimal(2.99), "A widget that is round");
    cart = new Cart();
    cart.setId(1l);
    cart.setItems(Arrays.asList(item));
    cart.setTotal(new BigDecimal(2.99));
    user.setCart(cart);
    cart.setUser(user);
    order = new UserOrder(1l, Arrays.asList(item), user, new BigDecimal(2.99));
  }

  @Test
  public void addToCartHappyPathTest() throws Exception {
    when(userRepository.findByUsername(USERNAME)).thenReturn(user);

    MockHttpServletResponse response = mockMvc.perform(
        post(PLACE_ORDER_URL + "/" + USERNAME).contentType("application/json")
            .header(HEADER_STRING, authenticationToken)).andExpect(status().isOk()).andReturn()
        .getResponse();
    String responseText = response.getContentAsString();
    UserOrder userOrder = mapper.readValue(responseText, UserOrder.class);
    assertEquals(1, userOrder.getItems().size());
    assertEquals(new BigDecimal(2.99), userOrder.getTotal());
  }

  @Test
  public void addToCartErrorPathTest() throws Exception {

    // 404 returned if nonRegistered user tries to place order
    when(userRepository.findByUsername(USERNAME)).thenReturn(null);
    mockMvc.perform(post(PLACE_ORDER_URL + "/" + USERNAME).contentType("application/json")
        .header(HEADER_STRING, authenticationToken)).andExpect(status().isNotFound());

    //0 order amount returned for an empty cart
    cart.setItems(new ArrayList<>());
    cart.setTotal(new BigDecimal(0));
    when(userRepository.findByUsername(USERNAME)).thenReturn(user);
    MockHttpServletResponse response = mockMvc.perform(
        post(PLACE_ORDER_URL + "/" + USERNAME).contentType("application/json")
            .header(HEADER_STRING, authenticationToken)).andExpect(status().isOk()).andReturn()
        .getResponse();
    String responseText = response.getContentAsString();
    UserOrder userOrder = mapper.readValue(responseText, UserOrder.class);
    assertEquals(0, userOrder.getItems().size());
    assertEquals(new BigDecimal(0), userOrder.getTotal());

  }

  @Test
  public void getOrdersForUserHappyPathTest() throws Exception {
    when(userRepository.findByUsername(USERNAME)).thenReturn(user);
    when(orderRepository.findByUser(user)).thenReturn(Arrays.asList(order));
    MockHttpServletResponse response = mockMvc.perform(
        get(HISTORY_URL + "/" + USERNAME).contentType("application/json")
            .header(HEADER_STRING, authenticationToken)).andExpect(status().isOk()).andReturn()
        .getResponse();
    String responseText = response.getContentAsString();
    List<UserOrder> userOrder =
        mapper.readValue(responseText, new TypeReference<List<UserOrder>>() {
        });
    assertEquals(1, userOrder.get(0).getItems().size());
    assertEquals(new BigDecimal(2.99), userOrder.get(0).getTotal());
  }

  @Test
  public void getOrdersForUserErrorPathTest() throws Exception {

    // 404 returned if nonRegistered user tries to search for order history
    when(userRepository.findByUsername(USERNAME)).thenReturn(null);
    mockMvc.perform(get(HISTORY_URL + "/" + USERNAME).contentType("application/json")
        .header(HEADER_STRING, authenticationToken)).andExpect(status().isNotFound());

    //Empty List returned when the user has yet not placed the order
    when(userRepository.findByUsername(USERNAME)).thenReturn(user);
    when(orderRepository.findByUser(user)).thenReturn(new ArrayList<>());
    MockHttpServletResponse response = mockMvc.perform(
        get(HISTORY_URL + "/" + USERNAME).contentType("application/json")
            .header(HEADER_STRING, authenticationToken)).andExpect(status().isOk()).andReturn()
        .getResponse();
    String responseText = response.getContentAsString();
    List<UserOrder> userOrder =
        mapper.readValue(responseText, new TypeReference<List<UserOrder>>() {
        });
    assertEquals(0, userOrder.size());


  }


}
