package ru.ssau.todo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ssau.todo.DTO.RegisterDTO;
import ru.ssau.todo.DTO.UserDTO;
import ru.ssau.todo.service.CustomUserDetailsService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final CustomUserDetailsService customUserDetailsService;

    public AuthController(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }
    @GetMapping("/me")
    public ResponseEntity<RegisterDTO> getUser(Authentication auth){
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        RegisterDTO registerDTO = customUserDetailsService.userToRegisteredUser(userDetails.getUsername());
        return ResponseEntity.ok(registerDTO);
    }
}
