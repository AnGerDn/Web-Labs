package ru.ssau.todo.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.exception.TaskNotFoundException;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
@Profile("jdbc")
public class TaskJdbcRepository implements TaskRepository{
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Task> taskRowMapper = (rs, rowNum) -> {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        task.setCreatedBy(rs.getLong("created_by"));
        task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return task;
    };

    public TaskJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public Task create(Task task) {
        String sql = "INSERT INTO task (title, status, created_by, created_at) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        LocalDateTime createAt = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getStatus().name());
            ps.setLong(3, task.getCreatedBy());
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(createAt));
            return ps;
        }, keyHolder);

        task.setId(keyHolder.getKey().longValue());
        task.setCreatedAt(createAt);

        return task;
    }

    @Override
    public Optional<Task> findById(long id) {
        String sql = "SELECT * FROM task WHERE id = ?";
        try {
            Task task = jdbcTemplate.queryForObject(sql, taskRowMapper, id);
            return Optional.ofNullable(task);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        String sql = "SELECT * FROM task WHERE created_by = ? AND created_at >= ? AND created_at <= ? ORDER BY created_at DESC";

        return jdbcTemplate.query(sql, taskRowMapper, userId,
                java.sql.Timestamp.valueOf(from),
                java.sql.Timestamp.valueOf(to));
    }

    @Override
    public void update(Task task) throws TaskNotFoundException {
        String sql = "UPDATE task SET title = ?, status = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql,
                task.getTitle(),
                task.getStatus().name(),
                task.getId()
        );
        if (updated == 0) {
            throw new TaskNotFoundException("Failed to update task with id " + task.getId());
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM task WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public long countActiveTasksByUserId(long userId) {
        String sql = "SELECT COUNT(*) FROM task WHERE created_by = ? AND status IN ('OPEN', 'IN_PROGRESS')";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId);
        return count != null ? count : 0L;
    }
}
