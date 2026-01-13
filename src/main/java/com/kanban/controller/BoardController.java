package com.kanban.controller;

import com.kanban.dto.BoardDTO;
import com.kanban.dto.CreateBoardRequest;
import com.kanban.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
    
    private final BoardService boardService;
    
    @PostMapping
    public ResponseEntity<BoardDTO> createBoard(@Valid @RequestBody CreateBoardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BoardDTO> getBoardById(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.getBoardById(id));
    }
    
    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<BoardDTO>> getBoardsByWorkspaceId(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(boardService.getBoardsByWorkspaceId(workspaceId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BoardDTO> updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody CreateBoardRequest request
    ) {
        return ResponseEntity.ok(boardService.updateBoard(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }
}

