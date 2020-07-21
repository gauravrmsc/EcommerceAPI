package com.gauravrmsc.ecommerce.model.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {

	@JsonProperty
	private String username;

	private String password;

	private String confirmPassword;
}
