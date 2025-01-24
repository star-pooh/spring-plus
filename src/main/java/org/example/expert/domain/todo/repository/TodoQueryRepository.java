package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class TodoQueryRepository {

    private final JPAQueryFactory queryFactory;

    public TodoResponse findByIdWithUser(Long todoId) {
        return queryFactory.select(Projections.constructor(TodoResponse.class,
                        todo.id,
                        todo.title,
                        todo.contents,
                        todo.weather,
                        Projections.constructor(UserResponse.class,
                                user.id,
                                user.email),
                        todo.createdAt,
                        todo.modifiedAt
                )).from(todo)
                .leftJoin(todo.user, user)
                .where(todo.id.eq(todoId))
                .fetchOne();
    }

    public Page<TodoSearchResponse> findByTitleAndNicknameAndCreatedAtDESC(Pageable pageable, String title, String nickname, LocalDateTime startDate, LocalDateTime endDate) {
        List<TodoSearchResponse> todos = queryFactory
                .select(Projections.constructor(TodoSearchResponse.class,
                        todo.title,
                        managerCountSubquery(),
                        commentCountSubquery()
                ))
                .from(todo)
                .innerJoin(todo.user, user)
                .where(combineConditions(title, nickname, startDate, endDate))
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        Long totalCount =
                Optional.ofNullable(
                                queryFactory
                                        .select(Wildcard.count)
                                        .from(todo)
                                        .innerJoin(todo.user, user)
                                        .where(combineConditions(title, nickname, startDate, endDate))
                                        .fetchOne())
                        .orElse(0L);

        return new PageImpl<>(todos, pageable, totalCount);
    }

    private Expression<Long> managerCountSubquery() {
        return JPAExpressions
                .select(Wildcard.count)
                .from(manager)
                .where(manager.todo.id.eq(todo.id));
    }

    private Expression<Long> commentCountSubquery() {
        return JPAExpressions
                .select(Wildcard.count)
                .from(comment)
                .where(comment.todo.id.eq(todo.id));
    }

    private BooleanBuilder combineConditions(String title, String nickname, LocalDateTime startDate, LocalDateTime endDate) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(likeTitle(title));
        booleanBuilder.and(likeNickname(nickname));
        booleanBuilder.and(goeCreatedAt(startDate));
        booleanBuilder.and(loeCreatedAt(endDate));

        return booleanBuilder;
    }

    private BooleanExpression goeCreatedAt(LocalDateTime createdAt) {
        return Objects.isNull(createdAt) ? null : todo.createdAt.goe(createdAt);
    }

    private BooleanExpression loeCreatedAt(LocalDateTime createdAt) {
        return Objects.isNull(createdAt) ? null : todo.createdAt.loe(createdAt);
    }

    private BooleanExpression likeTitle(String title) {
        return StringUtils.isBlank(title) ? null : todo.title.like("%" + title + "%");
    }

    private BooleanExpression likeNickname(String nickname) {
        return StringUtils.isBlank(nickname) ? null : todo.title.like("%" + nickname + "%");
    }
}
