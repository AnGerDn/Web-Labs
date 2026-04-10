package ru.ssau.todo.service;

import org.springframework.stereotype.Service;
import ru.ssau.todo.DTO.TaskDTO;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.exception.TaskNotFoundException;
import ru.ssau.todo.repository.TaskRepository;
import ru.ssau.todo.repository.UserRepository;
import ru.ssau.todo.entity.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private static final int MAX_ACTIVE_TASKS = 10;
    private static final int MIN_TASK_AGE_MINUTES = 5;
    private static final String ERROR_MAX_ACTIVE_TASKS = "User cannot have more than %d active tasks";

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private TaskDTO toDto(Task task){
        TaskDTO taskDTO = new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getCreatedBy().getId(),
                task.getCreatedAt()
        );
        return taskDTO;
    }
    private Task toEntity(TaskDTO taskDTO, User user){
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setStatus(taskDTO.getStatus());
        task.setCreatedBy(user);
        return task;
    }

    private void checkActiveTasksLimit(Long userId) {
        long activeCount = taskRepository.countActiveTasksByUserId(userId);
        if (activeCount >= MAX_ACTIVE_TASKS) {
            throw new IllegalStateException(
                    String.format(ERROR_MAX_ACTIVE_TASKS, MAX_ACTIVE_TASKS)
            );
        }
    }

    public TaskDTO createTask(TaskDTO taskDto) {
        if (taskDto.getCreatedBy() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        User user = userRepository.findById(taskDto.getCreatedBy())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + taskDto.getCreatedBy()));
        if(taskDto.getStatus() == TaskStatus.OPEN || taskDto.getStatus() == TaskStatus.IN_PROGRESS){
            checkActiveTasksLimit(taskDto.getCreatedBy());
        }
        Task task = toEntity(taskDto, user);
        Task created = taskRepository.save(task);
        return toDto(created);
    }

    public TaskDTO updateTask(TaskDTO taskDto) throws TaskNotFoundException {
        Optional<Task> existingTaskOpt = taskRepository.findById(taskDto.getId());
        if (existingTaskOpt.isEmpty()) {
            throw new TaskNotFoundException("Task not found with id: " + taskDto.getId());
        }

        Task existingTask = existingTaskOpt.get();

        // Если статус меняется на активный, проверяем лимит
        if ((taskDto.getStatus() == TaskStatus.OPEN || taskDto.getStatus() == TaskStatus.IN_PROGRESS) &&
                (existingTask.getStatus() == TaskStatus.DONE || existingTask.getStatus() == TaskStatus.CLOSED)) {
            checkActiveTasksLimit(existingTask.getCreatedBy().getId());
        }
        existingTask.setStatus(taskDto.getStatus());
        existingTask.setTitle(taskDto.getTitle());
        Task updated = taskRepository.save(existingTask);
        return toDto(updated);
    }

    public void deleteTask(Long id) {
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


    public Optional<TaskDTO> findById(Long id) {

        return taskRepository.findById(id)
                .map(this::toDto);
    }

    public List<TaskDTO> findAll(LocalDateTime from, LocalDateTime to, Long userId) {
        return taskRepository.findAll(from, to, userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public long countActiveTasks(Long userId) {
        return taskRepository.countActiveTasksByUserId(userId);
    }
}
