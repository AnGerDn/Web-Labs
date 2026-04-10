package ru.ssau.todo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.ssau.todo.entity.TaskStatus;

import java.time.LocalDateTime;

public class TaskDTO {
    private Long id;
    @NotBlank(message = "Заголовок не может быть пустым")
    private String title;
    @NotNull(message = "Статус не может быть пустым")
    private TaskStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;

    public TaskDTO() {}

    public TaskDTO(Long id, String title, TaskStatus status, Long createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
