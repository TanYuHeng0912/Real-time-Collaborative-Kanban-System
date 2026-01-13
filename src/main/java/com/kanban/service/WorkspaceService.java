package com.kanban.service;

import com.kanban.model.User;
import com.kanban.model.Workspace;
import com.kanban.repository.UserRepository;
import com.kanban.repository.WorkspaceRepository;
import com.kanban.util.SecurityUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
    
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateWorkspaceRequest {
        private String name;
        private String description;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkspaceDTO {
        private Long id;
        private String name;
        private String description;
        private Long ownerId;
    }
    
    @Transactional
    public WorkspaceDTO createWorkspace(CreateWorkspaceRequest request) {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(user)
                .isDeleted(false)
                .build();
        
        workspace = workspaceRepository.save(workspace);
        
        return new WorkspaceDTO(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getOwner().getId()
        );
    }
    
    @Transactional(readOnly = true)
    public List<WorkspaceDTO> getMyWorkspaces() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Workspace> workspaces = workspaceRepository.findByOwnerIdAndIsDeletedFalse(user.getId());
        return workspaces.stream()
                .map(w -> new WorkspaceDTO(w.getId(), w.getName(), w.getDescription(), w.getOwner().getId()))
                .collect(Collectors.toList());
    }
}

