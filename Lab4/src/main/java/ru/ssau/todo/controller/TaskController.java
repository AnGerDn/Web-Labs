package ru.ssau.todo.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.ssau.todo.DTO.TaskDTO;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.repository.UserRepository;
import ru.ssau.todo.service.TaskService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<TaskDTO> findAll(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to){
        LocalDateTime start = from != null ? from : LocalDateTime.of(1000,1,1,0,0,0);
        LocalDateTime end = to != null ? to : LocalDateTime.of(3000, 1, 1, 0,0,0);
        User user = getCurrentUser();
        return taskService.findAll(start, end, user.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> findById(@PathVariable Long id){
        Optional<TaskDTO> taskDto = taskService.findById(id);
        return taskDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping
    public ResponseEntity<TaskDTO> create(@Valid @RequestBody TaskDTO taskDto){
        User user = getCurrentUser();
        taskDto.setCreatedBy(user.getId());
        TaskDTO createdTask = taskService.createTask(taskDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTask.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdTask);
    }
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable Long id, @Valid @RequestBody TaskDTO taskDto){
        taskDto.setId(id);
        TaskDTO updated = taskService.updateTask(taskDto);
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/active/count")
    public long countActive(@RequestParam Long userId){
        return taskService.countActiveTasks(userId);
    }
}
