package com.kanban.controller;

import com.kanban.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
    
    private final WorkspaceService workspaceService;
    
    @PostMapping
    public ResponseEntity<WorkspaceService.WorkspaceDTO> createWorkspace(
            @RequestBody WorkspaceService.CreateWorkspaceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.createWorkspace(request));
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<WorkspaceService.WorkspaceDTO>> getMyWorkspaces() {
        return ResponseEntity.ok(workspaceService.getMyWorkspaces());
    }
}

