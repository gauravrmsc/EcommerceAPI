package com.gauravrmsc.ecommerce;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gauravrmsc.ecommerce.model.persistence.User;
import com.gauravrmsc.ecommerce.model.persistence.repositories.UserRepository;
import com.gauravrmsc.ecommerce.model.requests.CreateUserRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static com.gauravrmsc.ecommerce.security.SecurityConstants.HEADER_STRING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {ECommerceApplication.class})
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

  @MockBean
  UserRepository userRepository;
  @Autowired
  MockMvc mockMvc;
  private static final String BASE_URL = "/api/user";
  private static final String FIND_USER_BY_ID_URL = BASE_URL + "/id";
  private static final String CREATE_USER_ACCOUNT_URL = BASE_URL + "/create";
  private static final String authenticationToken =
      "BearereyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnYXVyYXYiLCJleHAiOjE1OTYxNjk0NDN9.xH82UbF3T_47uEYAo4m7QimX6CUo2XgnH3l7sSi5Olwdq_0zx9EtVrNKdIUfiOYbrlk7QU4dWlsdzg5CK46adw";
  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  private static final ObjectMapper mapper = new ObjectMapper();
  private static User user;

  @BeforeEach
  public void setup() {
    String hashedPassword = encoder.encode("987654321");
    user = new User("gaurav", hashedPassword);
  }

  @Test
  public void createUserAccountHappyPath() throws Exception {
    when(userRepository.findByUsername("gaurav")).thenReturn(null);
    when(userRepository.save(user)).thenReturn(user);
    CreateUserRequest createUserRequest = new CreateUserRequest("gaurav", "987654321", "987654321");
    String requestBody = mapper.writeValueAsString(createUserRequest);
    mockMvc
        .perform(post(CREATE_USER_ACCOUNT_URL).content(requestBody).contentType("application/json"))
        .andExpect(status().isOk());
  }

  @Test
  public void invalidCreateUserAccountRequestFails() throws Exception {
    CreateUserRequest createUserRequest = new CreateUserRequest("gaurav", "1234", "1234");
    String requestBody = mapper.writeValueAsString(createUserRequest);
    mockMvc
        .perform(post(CREATE_USER_ACCOUNT_URL).content(requestBody).contentType("application/json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void duplicateCreateUserRequestFails() throws Exception {
    when(userRepository.findByUsername("gaurav")).thenReturn(user);
    CreateUserRequest createUserRequest = new CreateUserRequest("gaurav", "987654321", "987654321");
    String requestBody = mapper.writeValueAsString(createUserRequest);
    mockMvc
        .perform(post(CREATE_USER_ACCOUNT_URL).content(requestBody).contentType("application/json"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void findByIdTest() throws Exception {
    //Malformed URL return 404
    mockMvc.perform(get(FIND_USER_BY_ID_URL).header(HEADER_STRING, authenticationToken))
        .andExpect(status().isNotFound());

    //Get User Query of unknown user returns NotFound
    when(userRepository.findById(any(long.class))).thenReturn(Optional.ofNullable(null));
    mockMvc.perform(get(FIND_USER_BY_ID_URL + "/1").header(HEADER_STRING, authenticationToken))
        .andExpect(status().isNotFound());

    //Get User Query for registered user returns user's details
    Long id = 1l;
    user.setId(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));
    mockMvc.perform(get(FIND_USER_BY_ID_URL + "/1").header(HEADER_STRING, authenticationToken))
        .andExpect(status().isOk()).andExpect(content().string(mapper.writeValueAsString(user)));
  }

  @Test
  public void findByUserNameTest() throws Exception {
    //Malformed URL return 404
    mockMvc.perform(get(BASE_URL).header(HEADER_STRING, authenticationToken))
        .andExpect(status().isNotFound());

    //Get User Query of unknown user returns NotFound
    when(userRepository.findByUsername(any(String.class))).thenReturn(null);
    mockMvc.perform(get(BASE_URL + "/gaurav").header(HEADER_STRING, authenticationToken))
        .andExpect(status().isNotFound());

    //Get User Query for registered user returns user's details
    Long id = 1l;
    user.setId(id);
    when(userRepository.findByUsername("gaurav")).thenReturn(user);
    mockMvc.perform(get(BASE_URL + "/gaurav").header(HEADER_STRING, authenticationToken))
        .andExpect(status().isOk()).andExpect(content().string(mapper.writeValueAsString(user)));
  }
}
