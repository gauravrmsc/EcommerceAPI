package com.gauravrmsc.ecommerce;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gauravrmsc.ecommerce.model.persistence.Item;
import com.gauravrmsc.ecommerce.model.persistence.repositories.ItemRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = {ECommerceApplication.class})
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
public class ItemControllerTest {

  @Autowired
  MockMvc mockMvc;
  @MockBean
  ItemRepository itemRepository;
  private static final String BASE_URL = "/api/item";
  private static final String FIND_ITEM_BY_NAME_URL = BASE_URL + "/name";
  private static final String authenticationToken =
      "BearereyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnYXVyYXYiLCJleHAiOjE1OTYxNjk0NDN9.xH82UbF3T_47uEYAo4m7QimX6CUo2XgnH3l7sSi5Olwdq_0zx9EtVrNKdIUfiOYbrlk7QU4dWlsdzg5CK46adw";
  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  private static final ObjectMapper mapper = new ObjectMapper();
  private static List<Item> items;
  private static Item item1;
  private static Item item2;

  @BeforeEach
  public void setup() {
    items = new ArrayList<>();
    item1 = new Item(1l, "Round Widget", new BigDecimal(2.99), "A widget that is round");
    item2 = new Item(2l, "Square Widget", new BigDecimal(1.99), "A widget that is square");
    items.add(item1);
    items.add(item2);
  }

  @Test
  public void findItemByIdHappyPath() throws Exception {
    when(itemRepository.findById(1l)).thenReturn(Optional.of(item1));
    mockMvc.perform(get(BASE_URL + "/1").header(HEADER_STRING, authenticationToken)
        .contentType("application/json")).andExpect(status().isOk())
        .andExpect(content().string(mapper.writeValueAsString(item1)));

  }

  @Test
  public void findItemByIdErrorPath() throws Exception {

    //Search for item not present in database return not found;
    when(itemRepository.findById(any(long.class))).thenReturn(Optional.empty());
    mockMvc.perform(get(BASE_URL + "/1").header(HEADER_STRING, authenticationToken)
        .contentType("application/json")).andExpect(status().isNotFound());

    //Access without token returns forbidden
    mockMvc.perform(get(BASE_URL + "/1").contentType("application/json"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void findAllItemErrorPath() throws Exception {
    //Access without token returns forbidden
    mockMvc.perform(get(BASE_URL).contentType("application/json"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void findAllItemsHappyPath() throws Exception {
    when(itemRepository.findAll()).thenReturn(items);
    mockMvc.perform(
        get(BASE_URL).header(HEADER_STRING, authenticationToken).contentType("application/json"))
        .andExpect(status().isOk()).andExpect(content().string(mapper.writeValueAsString(items)));
  }



  @Test
  public void findItemsByNameHappPath() throws Exception {
    when(itemRepository.findByName("Round Widget")).thenReturn(Arrays.asList(item1));
    mockMvc.perform(
        get(FIND_ITEM_BY_NAME_URL + "/Round Widget").header(HEADER_STRING, authenticationToken)
            .contentType("application/json")).andExpect(status().isOk())
        .andExpect(content().string(mapper.writeValueAsString(Arrays.asList(item1))));
  }

  @Test
  public void findItemByNameErrorPath() throws Exception {
    //Item not present in database returns 404
    when(itemRepository.findByName(any(String.class))).thenReturn(null);
    mockMvc.perform(
        get(FIND_ITEM_BY_NAME_URL + "/Round Widget").header(HEADER_STRING, authenticationToken)
            .contentType("application/json")).andExpect(status().isNotFound());

    //Access without token returns 403
    mockMvc.perform(get(FIND_ITEM_BY_NAME_URL + "/Round Widgit").contentType("application/json"))
        .andExpect(status().isForbidden());
  }
}
