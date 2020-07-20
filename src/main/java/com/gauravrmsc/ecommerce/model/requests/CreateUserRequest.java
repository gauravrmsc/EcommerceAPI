package com.gauravrmsc.ecommerce.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateUserRequest {

	@JsonProperty
	private String username;

	private String password;

	private String confirmPassword;
}
