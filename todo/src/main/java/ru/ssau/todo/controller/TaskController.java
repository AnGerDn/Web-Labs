package ru.ssau.todo.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.exception.TaskNotFoundException;
import ru.ssau.todo.repository.TaskRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private TaskRepository taskRepository;
    public TaskController(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }
    @GetMapping
    public List<Task> findAll(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                              @RequestParam long userId){
        LocalDateTime start = from != null ? from : LocalDateTime.MIN;
        LocalDateTime end = to != null ? to : LocalDateTime.MAX;
        return taskRepository.findAll(start, end, userId);
    }

    @GetMapping("/{id}")
    public Task findById(@PathVariable long id){
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task){
        Task createdTask = taskRepository.create(task);
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
        try{
            taskRepository.update(task);
        } catch (TaskNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Task> delete(@PathVariable long id){
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/active/count")
    public long countActive(@RequestParam long userId){
        long actTasks = taskRepository.countActiveTasksByUserId(userId);
        return actTasks;
    }
}
