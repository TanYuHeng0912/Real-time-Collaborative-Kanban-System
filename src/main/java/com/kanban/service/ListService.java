package com.kanban.service;

import com.kanban.dto.CreateListRequest;
import com.kanban.dto.ListDTO;
import com.kanban.dto.MoveListRequest;
import com.kanban.model.Board;
import com.kanban.model.Card;
import com.kanban.model.ListEntity;
import com.kanban.model.User;
import com.kanban.repository.BoardRepository;
import com.kanban.repository.ListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListService {
    
    private final ListRepository listRepository;
    private final BoardRepository boardRepository;
    private final PermissionService permissionService;
    
    @Transactional
    public ListDTO createList(CreateListRequest request) {
        permissionService.verifyBoardAccess(request.getBoardId());
        
        Board board = boardRepository.findByIdAndIsDeletedFalse(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found"));
        
        Integer position = request.getPosition();
        if (position == null) {
            List<ListEntity> existingLists = listRepository.findByBoardIdAndIsDeletedFalseOrderByPositionAsc(request.getBoardId());
            position = existingLists.isEmpty() ? 0 : existingLists.get(existingLists.size() - 1).getPosition() + 1;
        }
        
        ListEntity list = ListEntity.builder()
                .name(request.getName())
                .board(board)
                .position(position)
                .isDeleted(false)
                .build();
        
        list = listRepository.save(list);
        return toDTO(list);
    }
    
    @Transactional(readOnly = true)
    public ListDTO getListById(Long id) {
        ListEntity list = listRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("List not found"));
        return toDTO(list);
    }
    
    @Transactional(readOnly = true)
    public List<ListDTO> getListsByBoardId(Long boardId) {
        List<ListEntity> lists = listRepository.findByBoardIdAndIsDeletedFalseOrderByPositionAsc(boardId);
        return lists.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ListDTO updateList(Long id, CreateListRequest request) {
        User currentUser = permissionService.getCurrentUser();
        
        if (!permissionService.canEditList(id, currentUser)) {
            throw new AccessDeniedException("You do not have permission to edit this list.");
        }
        
        ListEntity list = listRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("List not found"));
        
        list.setName(request.getName());
        if (request.getPosition() != null) {
            list.setPosition(request.getPosition());
        }
        
        list = listRepository.save(list);
        return toDTO(list);
    }
    
    @Transactional
    public void deleteList(Long id) {
        User currentUser = permissionService.getCurrentUser();
        
        if (!permissionService.canDeleteList(id, currentUser)) {
            throw new AccessDeniedException("You do not have permission to delete this list.");
        }
        
        ListEntity list = listRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("List not found"));
        
        list.setIsDeleted(true);
        listRepository.save(list);
    }
    
    @Transactional
    public ListDTO moveList(Long id, MoveListRequest request) {
        // Fetch list with board to avoid lazy loading issues
        ListEntity list = listRepository.findByIdWithBoard(id)
                .orElseThrow(() -> new RuntimeException("List not found"));
        
        if (list.getBoard() == null) {
            throw new RuntimeException("Board not found for list");
        }
        
        // Get board ID
        Long boardId = list.getBoard().getId();
        
        // Fetch board with workspace to avoid lazy loading issues
        Board board = boardRepository.findByIdWithWorkspace(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        
        if (board.getWorkspace() == null) {
            throw new RuntimeException("Workspace not found for board");
        }
        
        // Only workspace owner/admin can reorder lists
        User currentUser = permissionService.getCurrentUser();
        if (!permissionService.isWorkspaceOwnerOrAdmin(board.getWorkspace().getId(), currentUser)) {
            throw new AccessDeniedException("Only workspace owners or admins can reorder lists.");
        }
        
        Integer oldPosition = list.getPosition();
        Integer newPosition = request.getNewPosition();
        
        if (oldPosition != null && oldPosition.equals(newPosition)) {
            return toDTO(list);
        }
        
        List<ListEntity> allLists = listRepository.findByBoardIdAndIsDeletedFalseOrderByPositionAsc(boardId);
        
        // Remove the moving list from the list
        allLists.removeIf(l -> l.getId().equals(id));
        
        // Insert at new position
        if (newPosition == null) {
            newPosition = allLists.size();
        } else if (newPosition < 0) {
            newPosition = 0;
        } else if (newPosition >= allLists.size()) {
            newPosition = allLists.size();
        }
        
        allLists.add(newPosition, list);
        
        // Update positions for all affected lists
        for (int i = 0; i < allLists.size(); i++) {
            allLists.get(i).setPosition(i);
        }
        
        List<ListEntity> savedLists = listRepository.saveAll(allLists);
        // Find the updated list from saved lists
        ListEntity updatedList = savedLists.stream()
                .filter(l -> l.getId().equals(id))
                .findFirst()
                .orElse(list);
        
        // Fetch the list again with cards to avoid lazy loading issues in toDTO
        ListEntity refreshedList = listRepository.findByIdWithCards(updatedList.getId())
                .orElse(updatedList);
        
        return toDTO(refreshedList);
    }
    
    private ListDTO toDTO(ListEntity list) {
        try {
            return ListDTO.builder()
                    .id(list.getId())
                    .name(list.getName())
                    .boardId(list.getBoard() != null ? list.getBoard().getId() : null)
                    .position(list.getPosition())
                    .createdAt(list.getCreatedAt())
                    .updatedAt(list.getUpdatedAt())
                    .cards(list.getCards() != null ? list.getCards().stream()
                            .filter(card -> !card.getIsDeleted())
                            .map(this::cardToDTO)
                            .collect(Collectors.toList()) : List.of())
                    .build();
        } catch (Exception e) {
            // If lazy loading fails, return DTO without cards
            return ListDTO.builder()
                    .id(list.getId())
                    .name(list.getName())
                    .boardId(list.getBoard() != null ? list.getBoard().getId() : null)
                    .position(list.getPosition())
                    .createdAt(list.getCreatedAt())
                    .updatedAt(list.getUpdatedAt())
                    .cards(List.of())
                    .build();
        }
    }
    
    private com.kanban.dto.CardDTO cardToDTO(Card card) {
        return com.kanban.dto.CardDTO.builder()
                .id(card.getId())
                .title(card.getTitle())
                .description(card.getDescription())
                .listId(card.getList().getId())
                .position(card.getPosition())
                .createdBy(card.getCreatedBy().getId())
                .assignedTo(card.getAssignedTo() != null ? card.getAssignedTo().getId() : null)
                .dueDate(card.getDueDate())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }
}

