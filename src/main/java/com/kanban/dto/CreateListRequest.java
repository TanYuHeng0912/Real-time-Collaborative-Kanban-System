package com.kanban.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateListRequest {
    @NotBlank(message = "List name is required")
    private String name;
    @NotNull(message = "Board ID is required")
    private Long boardId;
    private Integer position;
}

