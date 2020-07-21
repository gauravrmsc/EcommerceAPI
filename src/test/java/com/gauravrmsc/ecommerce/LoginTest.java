package com.gauravrmsc.ecommerce;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gauravrmsc.ecommerce.model.persistence.User;
import com.gauravrmsc.ecommerce.model.persistence.repositories.UserRepository;
import com.gauravrmsc.ecommerce.model.requests.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = {ECommerceApplication.class})
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class LoginTest {
  private static final String loginURI = "/login";
  private static User user;
  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  private static final ObjectMapper mapper = new ObjectMapper();
  @MockBean
  UserRepository userRepository;
  @Autowired
  MockMvc mockMcv;

  @BeforeEach
  public void setup() {
    String hashedPassword = encoder.encode("98765");
    user = new User("gauravrmsc", hashedPassword);
  }

  @Test
  public void badLoginRequestFails() throws Exception {
    LoginRequest loginRequest = new LoginRequest("gauravrmsc", "3687");
    when((Iterable<? extends Publisher<?>>) userRepository.findByUsername(any(String.class)))
        .thenReturn(null);

    //Unauthorized returned When the user is not registered
    String requestBody = mapper.writeValueAsString(loginRequest);
    MockHttpServletResponse response =
        mockMcv.perform(post(loginURI).content(requestBody)).andReturn().getResponse();
    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());

    //Unauthorized returned when password is incorrect
    when(userRepository.findByUsername("gauravrmsc")).thenReturn(user);
    mockMcv.perform(post(loginURI).content(requestBody)).andExpect(status().isUnauthorized());
  }

  @Test
  public void validLoginRequestSucceeds() throws Exception {
    LoginRequest loginRequest = new LoginRequest("gauravrmsc", "98765");
    when(userRepository.findByUsername("gauravrmsc")).thenReturn(user);
    String requestBody = mapper.writeValueAsString(loginRequest);
    MockHttpServletResponse response =
        mockMcv.perform(post(loginURI).content(requestBody)).andReturn().getResponse();
    mockMcv.perform(post(loginURI).content(requestBody)).andExpect(status().isOk());
  }
}
