package org.example.expert.domain.comment.dto.response;

import lombok.Getter;

@Getter
public class CommentResponse {

    private final Long id;
    private final String contents;
    private final Long userId;
    private final String email;

    public CommentResponse(Long id, String contents, Long userId, String email) {
        this.id = id;
        this.contents = contents;
        this.userId = userId;
        this.email = email;
    }
}
