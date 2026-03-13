package ru.ssau.todo.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.exception.TaskNotFoundException;
import ru.ssau.todo.service.TaskService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> findAll(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                              @RequestParam long userId){
        LocalDateTime start = from != null ? from : LocalDateTime.of(1000,1,1,0,0,0);
        LocalDateTime end = to != null ? to : LocalDateTime.of(3000, 1, 1, 0,0,0);
        return taskService.findAll(start, end, userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> findById(@PathVariable long id){
        Optional<Task> task = taskService.findById(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task){
        Task createdTask = taskService.createTask(task);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTask.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdTask);
    }
    @PutMapping("/{id}")
    public void update(@PathVariable long id, @RequestBody Task task){
        task.setId(id);
        try {
            taskService.updateTask(task);
        } catch (TaskNotFoundException e) {
            throw new TaskNotFoundException( e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Task> delete(@PathVariable long id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/active/count")
    public long countActive(@RequestParam long userId){
        return taskService.countActiveTasks(userId);
    }
}
