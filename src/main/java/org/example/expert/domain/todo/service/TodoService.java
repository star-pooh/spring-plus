package org.example.expert.domain.todo.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.log.service.LogService;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoQueryRepository;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoQueryRepository todoQueryRepository;
    private final WeatherClient weatherClient;
    private final LogService logService;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        // Cascade 연관 관계로 할 일을 등록할 때 Manager가 등록되는 경우에도 Log가 등록될 수 있도록 트랜잭션 동기화 콜백 등록
        // 이벤트 리스너를 통해 스프링이 트랜잭션 종료를 감지할 수 있도록 하는 것도 가능
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                boolean isSuccess = (status == TransactionSynchronization.STATUS_COMMITTED);
                logService.saveLog(isSuccess);
            }
        });

        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    public Page<TodoResponse> getTodos(int page, int size, String weather, String modifiedAtStart, String modifiedAtEnd) {
        Pageable pageable = PageRequest.of(page - 1, size);

        LocalDateTime startDate = StringUtils.isBlank(modifiedAtStart) ? null
                : LocalDateTime.parse(modifiedAtStart.concat("T00:00:00"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endDate = StringUtils.isBlank(modifiedAtEnd) ? null
                : LocalDateTime.parse(modifiedAtEnd.concat("T23:59:59"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Page<Todo> todos = todoRepository.findAllByWeatherAndModifiedAtDesc(pageable, weather, startDate, endDate);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        return Optional.ofNullable(todoQueryRepository.findByIdWithUser(todoId))
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));
    }

    public Page<TodoSearchResponse> searchTodos(int pageNum, int pageSize, String title, String nickname, String createdAtStart, String createdAtEnd) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        LocalDateTime startDate = StringUtils.isBlank(createdAtStart) ? null
                : LocalDateTime.parse(createdAtStart.concat("T00:00:00"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endDate = StringUtils.isBlank(createdAtEnd) ? null
                : LocalDateTime.parse(createdAtEnd.concat("T23:59:59"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return todoQueryRepository.findByTitleAndNicknameAndCreatedAtDESC(pageable, title, nickname, startDate, endDate);
    }
}
