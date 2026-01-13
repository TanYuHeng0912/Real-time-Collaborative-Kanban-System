package com.kanban.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveCardRequest {
    @NotNull(message = "Target list ID is required")
    private Long targetListId;
    @NotNull(message = "New position is required")
    private Integer newPosition;
}

