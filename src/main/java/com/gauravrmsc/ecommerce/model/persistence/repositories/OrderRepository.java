package com.gauravrmsc.ecommerce.model.persistence.repositories;

import com.gauravrmsc.ecommerce.model.persistence.UserOrder;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gauravrmsc.ecommerce.model.persistence.User;

public interface OrderRepository extends JpaRepository<UserOrder, Long> {
	List<UserOrder> findByUser(User user);
}
