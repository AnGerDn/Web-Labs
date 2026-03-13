package ru.ssau.todo.service;

import org.springframework.stereotype.Service;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.exception.TaskNotFoundException;
import ru.ssau.todo.repository.TaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private static final int MAX_ACTIVE_TASKS = 10;
    private static final int MIN_TASK_AGE_MINUTES = 5;
    private static final String ERROR_MAX_ACTIVE_TASKS = "User cannot have more than %d active tasks";

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    private void checkActiveTasksLimit(Long userId) {
        long activeCount = taskRepository.countActiveTasksByUserId(userId);
        if (activeCount >= MAX_ACTIVE_TASKS) {
            throw new IllegalStateException(
                    String.format(ERROR_MAX_ACTIVE_TASKS, MAX_ACTIVE_TASKS)
            );
        }
    }

    public Task createTask(Task task) {
        if(task.getStatus() == TaskStatus.OPEN || task.getStatus() == TaskStatus.IN_PROGRESS){
            checkActiveTasksLimit(task.getCreatedBy());
        }
        return taskRepository.create(task);
    }

    public void updateTask(Task task) throws TaskNotFoundException {
        Optional<Task> existingTaskOpt = taskRepository.findById(task.getId());
        if (existingTaskOpt.isEmpty()) {
            throw new TaskNotFoundException("Task not found with id: " + task.getId());
        }

        Task existingTask = existingTaskOpt.get();

        // Если статус меняется на активный, проверяем лимит
        if ((task.getStatus() == TaskStatus.OPEN || task.getStatus() == TaskStatus.IN_PROGRESS) &&
                (existingTask.getStatus() == TaskStatus.DONE || existingTask.getStatus() == TaskStatus.CLOSED)) {
            checkActiveTasksLimit(task.getCreatedBy());
        }

        taskRepository.update(task);
    }

    public void deleteTask(long id) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }

        Task task = taskOpt.get();
        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = ChronoUnit.MINUTES.between(task.getCreatedAt(), now);

        // Проверка 2: нельзя удалять задачи младше 5 минут
        if (minutesElapsed < MIN_TASK_AGE_MINUTES) {
            throw new IllegalStateException(
                    "Cannot delete task created less than 5 minutes ago. " +
                            "Elapsed time: " + minutesElapsed + " minutes");
        }

        taskRepository.deleteById(id);
    }


    public Optional<Task> findById(long id) {
        return taskRepository.findById(id);
    }

    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        return taskRepository.findAll(from, to, userId);
    }

    public long countActiveTasks(long userId) {
        return taskRepository.countActiveTasksByUserId(userId);
    }
}
