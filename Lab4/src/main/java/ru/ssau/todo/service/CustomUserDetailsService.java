package ru.ssau.todo.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.todo.DTO.RegisterDTO;
import ru.ssau.todo.DTO.UserDTO;
import ru.ssau.todo.entity.Role;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.repository.RoleRepository;
import ru.ssau.todo.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->new UsernameNotFoundException("User not found - "+ username));
        // Преобразуем роли в формат, понятный Spring Security
        Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        // Возвращаем объект UserDetails, который Spring Security будет использовать
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
    public UserDTO registerUsr(UserDTO userDto){
        if(userRepository.findByUsername(userDto.getUsername()).isPresent()){
            throw new IllegalArgumentException("User with name " + userDto.getUsername() + " exist");
        }
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        String roleName = "admin".equalsIgnoreCase(userDto.getUsername()) ? "ROLE_ADMIN" : "ROLE_USER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found"));
        user.setRoles(Collections.singletonList(role));

        User saved = userRepository.save(user);
        UserDTO savedDto = new UserDTO();
        savedDto.setUsername(saved.getUsername());
        savedDto.setId(saved.getId());
        return savedDto;

    }

    public RegisterDTO userToRegisteredUser(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->new UsernameNotFoundException("User not found - "+ username));
        RegisterDTO registered = new RegisterDTO();
        registered.setUsername(user.getUsername());
        for (Role role: user.getRoles()) {
            registered.getRoles().add(role.getName());
        }
        registered.setId(user.getId());
        return registered;
    }
}
