package com.api.apispringbootexceptionhandler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.apispringbootexceptionhandler.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
