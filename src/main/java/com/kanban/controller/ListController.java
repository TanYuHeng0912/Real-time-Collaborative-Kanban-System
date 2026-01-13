package com.kanban.controller;

import com.kanban.dto.CreateListRequest;
import com.kanban.dto.ListDTO;
import com.kanban.service.ListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lists")
@RequiredArgsConstructor
public class ListController {
    
    private final ListService listService;
    
    @PostMapping
    public ResponseEntity<ListDTO> createList(@Valid @RequestBody CreateListRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listService.createList(request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ListDTO> getListById(@PathVariable Long id) {
        return ResponseEntity.ok(listService.getListById(id));
    }
    
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<ListDTO>> getListsByBoardId(@PathVariable Long boardId) {
        return ResponseEntity.ok(listService.getListsByBoardId(boardId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ListDTO> updateList(
            @PathVariable Long id,
            @Valid @RequestBody CreateListRequest request
    ) {
        return ResponseEntity.ok(listService.updateList(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteList(@PathVariable Long id) {
        listService.deleteList(id);
        return ResponseEntity.noContent().build();
    }
}

