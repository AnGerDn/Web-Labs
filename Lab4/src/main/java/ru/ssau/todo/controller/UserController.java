package ru.ssau.todo.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ssau.todo.DTO.UserDTO;
import ru.ssau.todo.service.CustomUserDetailsService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final CustomUserDetailsService customUserDetailsService;

    public UserController(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDto){
        return ResponseEntity.ok(customUserDetailsService.registerUsr(userDto));
    }
}
