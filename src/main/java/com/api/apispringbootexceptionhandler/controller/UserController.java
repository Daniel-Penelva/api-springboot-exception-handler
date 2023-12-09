package com.api.apispringbootexceptionhandler.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.apispringbootexceptionhandler.entity.User;
import com.api.apispringbootexceptionhandler.exceptions.ResourceNotFoundException;
import com.api.apispringbootexceptionhandler.repository.UserRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // get all users - localhost:8080/api/users/all
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    // get user by id - localhost:8080/api/users/search/{id}
    @GetMapping("/search/{id}")
    public User getUserById(@PathVariable(value = "id") @NotNull @Positive long userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id :: " + userId));
    }

    // create user - localhost:8080/api/users/create
    @PostMapping("/create")
    public User createUser(@RequestBody @Valid User user) {
        return this.userRepository.save(user);
    }

    // update user - localhost:8080/api/users/replace/{id}
    @PutMapping("/replace/{id}")
    public User updateUser(@RequestBody @Valid User user, @PathVariable("id") @NotNull @Positive long userId) {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id :: " + userId));

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());

        return this.userRepository.save(existingUser);
    }

    // delete user by id - localhost:8080/api/users/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable("id") @NotNull @Positive long userId) {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id :: " + userId));

        this.userRepository.delete(existingUser);
        return ResponseEntity.ok().build();
    }

}
