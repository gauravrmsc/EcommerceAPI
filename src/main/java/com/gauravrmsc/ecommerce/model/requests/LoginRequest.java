package com.gauravrmsc.ecommerce.model.requests;

import lombok.Data;

@Data
public class LoginRequest {
  String username;
  String password;
}
