package org.example.expert.domain.comment.repository;

import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
            SELECT new org.example.expert.domain.comment.dto.response.CommentResponse(
                c.id,
                c.contents,
                u.id,
                u.email)
            FROM Comment c
            JOIN User u
            ON c.user.id = u.id
            WHERE c.todo.id = :todoId
            """)
    List<CommentResponse> findByTodoIdWithUser(Long todoId);
}
