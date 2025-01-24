package org.example.expert.domain.todo.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TodoSearchResponse {
    private final String title;
    private final Long managerNum;
    private final Long commentNum;
}
