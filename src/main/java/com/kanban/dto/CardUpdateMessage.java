package com.kanban.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardUpdateMessage {
    private String type; // "CREATED", "UPDATED", "MOVED", "DELETED"
    private CardDTO card;
    private Long boardId;
    private Long previousListId; // For move operations
    private Long cardId; // For DELETE operations
}
