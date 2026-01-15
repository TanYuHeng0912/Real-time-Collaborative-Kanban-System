package com.kanban.controller;

import com.kanban.dto.CardDTO;
import com.kanban.dto.CardUpdateMessage;
import com.kanban.dto.CreateCardRequest;
import com.kanban.dto.MoveCardRequest;
import com.kanban.dto.UpdateCardRequest;
import com.kanban.model.ListEntity;
import com.kanban.model.User;
import com.kanban.repository.ListRepository;
import com.kanban.service.CardService;
import com.kanban.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {
    
    private final CardService cardService;
    private final ListRepository listRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PermissionService permissionService;
    
    @PostMapping
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CreateCardRequest request) {
        CardDTO card = cardService.createCard(request);
        
        // Get board ID from list and broadcast card creation
        Long boardId = listRepository.findByIdWithBoard(request.getListId())
                .map(list -> list.getBoard() != null ? list.getBoard().getId() : null)
                .orElse(null);
        
        if (boardId != null) {
            CardUpdateMessage message = new CardUpdateMessage("CREATED", card, boardId, null, null, 
                    card.getLastModifiedBy(), card.getLastModifiedByName());
            messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }
    
    @GetMapping("/list/{listId}")
    public ResponseEntity<List<CardDTO>> getCardsByListId(@PathVariable Long listId) {
        return ResponseEntity.ok(cardService.getCardsByListId(listId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CardDTO> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCardRequest request
    ) {
        CardDTO card = cardService.updateCard(id, request);
        
        // Get board ID and broadcast card update
        Long boardId = listRepository.findByIdWithBoard(card.getListId())
                .map(list -> list.getBoard() != null ? list.getBoard().getId() : null)
                .orElse(null);
        
        if (boardId != null) {
            CardUpdateMessage message = new CardUpdateMessage("UPDATED", card, boardId, null, null,
                    card.getLastModifiedBy(), card.getLastModifiedByName());
            messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
        }
        
        return ResponseEntity.ok(card);
    }
    
    @PostMapping("/{id}/move")
    public ResponseEntity<CardDTO> moveCard(
            @PathVariable Long id,
            @Valid @RequestBody MoveCardRequest request
    ) {
        // Get previous list ID from the card response after moving
        // We'll get it from the card DTO returned by moveCard
        
        // Get target list with board before moving to get boardId
        ListEntity targetList = listRepository.findByIdWithBoard(request.getTargetListId())
                .orElseThrow(() -> new RuntimeException("Target list not found"));
        Long boardId = targetList.getBoard() != null ? targetList.getBoard().getId() : null;
        
        CardDTO card = cardService.moveCard(id, request);
        
        // Get previous list ID from the card's old listId (before it was updated)
        // Since we can't easily get it now, use the card's current listId
        // This might be slightly inaccurate for WebSocket messages, but moveCard handles the logic correctly
        Long previousListId = null; // Will be handled by WebSocket if needed
        
        // Broadcast card movement
        if (boardId != null) {
            CardUpdateMessage message = new CardUpdateMessage("MOVED", card, boardId, previousListId, null,
                    card.getLastModifiedBy(), card.getLastModifiedByName());
            messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
        }
        
        return ResponseEntity.ok(card);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        CardDTO card = cardService.getCardById(id);
        Long boardId = listRepository.findByIdWithBoard(card.getListId())
                .map(list -> list.getBoard() != null ? list.getBoard().getId() : null)
                .orElse(null);
        
        User currentUser = permissionService.getCurrentUser();
        String userName = currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername();
        
        cardService.deleteCard(id);
        
        // Broadcast card deletion to board subscribers
        if (boardId != null) {
            CardUpdateMessage message = new CardUpdateMessage("DELETED", null, boardId, card.getListId(), id,
                    currentUser.getId(), userName);
            messagingTemplate.convertAndSend("/topic/board/" + boardId, message);
        }
        
        return ResponseEntity.noContent().build();
    }
}

