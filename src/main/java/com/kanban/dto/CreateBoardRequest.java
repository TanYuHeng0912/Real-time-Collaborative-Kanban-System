package com.kanban.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBoardRequest {
    @NotBlank(message = "Board name is required")
    private String name;
    private String description;
    @NotNull(message = "Workspace ID is required")
    private Long workspaceId;
}

