package com.org.coop.society.data.customer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.coop.society.data.customer.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	public User findByUserName(String userName);
}
