package ru.ssau.todo.repository;

import org.springframework.stereotype.Repository;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.exception.TaskNotFoundException;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public class TaskInMemoryRepository implements TaskRepository{
    private final Map<Long, Task> tasks = new HashMap<>();
    private long curId = 1;
    @Override
    public Task create(Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().isBlank() || task.getStatus() == null){
            throw new IllegalArgumentException();
        }
        task.setId(curId);
        curId++;
        task.setCreatedAt(LocalDateTime.now());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        List<Task> resTasks = new ArrayList<>();
        for(Task task : tasks.values()){
            if(task.getCreatedBy() == userId){
                if(task.getCreatedAt() != null && !task.getCreatedAt().isBefore(from)
                        && !task.getCreatedAt().isAfter(to)){
                    resTasks.add(task);
                }
            }
        }
        return resTasks;
    }

    @Override
    public void update(Task task) throws TaskNotFoundException {
        if (tasks.get(task.getId()) == null){
            throw new TaskNotFoundException("this task not exist");
        }
        Task updatedTask = tasks.get(task.getId());
        updatedTask.setStatus(task.getStatus());
        updatedTask.setTitle(task.getTitle());
    }

    @Override
    public void deleteById(long id) {
        tasks.remove(id);
    }

    @Override
    public long countActiveTasksByUserId(long userId) {
        long actTasks = 0;
        for(Task task : tasks.values()){
            if(task.getCreatedBy() == userId){
                if(task.getStatus() == TaskStatus.OPEN || task.getStatus() == TaskStatus.IN_PROGRESS){
                    actTasks++;
                }
            }
        }
        return actTasks;
    }
}
