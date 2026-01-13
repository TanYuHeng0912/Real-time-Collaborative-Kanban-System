package com.kanban.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDTO {
    private Long id;
    private String name;
    private Long boardId;
    private Integer position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CardDTO> cards;
}

