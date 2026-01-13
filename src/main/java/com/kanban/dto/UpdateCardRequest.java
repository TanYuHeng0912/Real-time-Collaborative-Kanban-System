package com.kanban.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCardRequest {
    private String title;
    private String description;
    private Long listId;
    private Integer position;
    private Long assignedTo;
    private LocalDateTime dueDate;
}

