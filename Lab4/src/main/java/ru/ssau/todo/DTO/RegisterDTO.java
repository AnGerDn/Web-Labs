package ru.ssau.todo.DTO;

import java.util.ArrayList;
import java.util.List;

public class RegisterDTO {
    private Long id;
    private String username;
    private List<String> roles = new ArrayList<>();

    public RegisterDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
