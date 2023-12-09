package com.api.apispringbootexceptionhandler.controller;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.apispringbootexceptionhandler.entity.User;
import com.api.apispringbootexceptionhandler.errors.ErrorDetails;
import com.api.apispringbootexceptionhandler.exceptions.ResourceNotFoundException;
import com.api.apispringbootexceptionhandler.repository.UserRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/users")
@Validated   // A anotação @Validated ativa a validação global para a classe UserController - permite que a validação seja realizada automaticamente nos métodos que usam @Valid ou @Validated
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Validator validator;  // A função do Validator é realizar validação de objetos em conformidade com as anotações de validação (como @NotNull, @NotBlank, etc.) que estão presentes nos campos da classe User. 


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

    // create user (exemplo 1 - utilizando a anotação @Valid) - localhost:8080/api/users/create
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        
        /* Verifica se há erros usando o bindingResult */
        if (bindingResult.hasErrors()) {
            return handleValidationErrorsBindingResult(bindingResult);
        }

        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }


     /* Percorre todos os erros de validação e os coleta em uma lista de mensagens de erros. O objeto ErrorDetails contém a mensagem de erro e retorna a resposta com status BAD_REQUEST. */
    private ResponseEntity<?> handleValidationErrorsBindingResult(BindingResult bindingResult) {
        
        List<String> errors = bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorDetails errorDetails = new ErrorDetails(new Date(), "Validation Error", errors.toString());

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }


    // create user (exemplo 2 - utilizando o Validator) - localhost:8080/api/users/createUser
    @PostMapping("/createUser")
    public ResponseEntity<?> createUser2(@RequestBody User user, BindingResult bindingResult) {
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            return handleValidationErrorsConstraintViolation(violations);
        }

        try {
            User createdUser = userRepository.save(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception ex) {
            // Trate exceções específicas do seu código aqui, se necessário
            return new ResponseEntity<>(new ErrorDetails(new Date(), "Error during user creation", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /* Este método recebe um conjunto de violações de validação e processa essas violações para construir uma resposta com mensagens de erro 
      apropriadas. As mensagens de erro são coletadas e encapsuladas em um objeto ErrorDetails.  */
    private ResponseEntity<?> handleValidationErrorsConstraintViolation(Set<ConstraintViolation<User>> violations) {
        
        List<String> errors = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ErrorDetails errorDetails = new ErrorDetails(new Date(), "Validation Error", errors.toString());
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    
    // update user (utilizando o validator) - localhost:8080/api/users/replace/{id}
    @PutMapping("/replace/{id}")
    public ResponseEntity<?> updateUser(
            @RequestBody User user,
            @PathVariable("id") @NotNull @Positive long userId) {

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            return handleValidationErrorsConstraintViolation(violations);
        }

        try {
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id :: " + userId));

            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setEmail(user.getEmail());

            User updatedUser = userRepository.save(existingUser);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(new ErrorDetails(new Date(), ex.getMessage(), ""), HttpStatus.NOT_FOUND);
        }
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
