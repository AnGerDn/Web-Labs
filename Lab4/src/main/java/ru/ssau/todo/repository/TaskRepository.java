package ru.ssau.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.exception.TaskNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для управления жизненным циклом сущностей {@link Task}.
 * Обеспечивает абстракцию над механизмом хранения данных.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(value = """
            SELECT * FROM task
            WHERE created_by = :userId
            AND created_at BETWEEN :from AND :to""",
            nativeQuery = true)
    List<Task> findAll(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.createdBy.id = :userId AND t.status IN ('OPEN', 'IN_PROGRESS')")
    long countActiveTasksByUserId(@Param("userId") Long userId);
}