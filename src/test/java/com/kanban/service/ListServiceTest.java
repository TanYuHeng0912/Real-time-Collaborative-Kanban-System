package com.kanban.service;

import com.kanban.dto.CreateListRequest;
import com.kanban.dto.ListDTO;
import com.kanban.dto.MoveListRequest;
import com.kanban.model.Board;
import com.kanban.model.ListEntity;
import com.kanban.model.User;
import com.kanban.model.Workspace;
import com.kanban.model.WorkspaceMember;
import com.kanban.repository.BoardRepository;
import com.kanban.repository.ListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListServiceTest {

    @Mock
    private ListRepository listRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private ListService listService;

    private User testUser;
    private Workspace workspace;
    private Board board;
    private ListEntity list;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .role(User.UserRole.USER)
                .isDeleted(false)
                .build();

        workspace = Workspace.builder()
                .id(1L)
                .name("Test Workspace")
                .owner(testUser)
                .isDeleted(false)
                .build();

        board = Board.builder()
                .id(1L)
                .name("Test Board")
                .workspace(workspace)
                .createdBy(testUser)
                .isDeleted(false)
                .build();

        list = ListEntity.builder()
                .id(1L)
                .name("To Do")
                .board(board)
                .position(0)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateList_Success() {
        CreateListRequest request = new CreateListRequest();
        request.setName("New List");
        request.setBoardId(1L);
        request.setPosition(null); // Auto-calculate position

        doNothing().when(permissionService).verifyBoardAccess(1L);
        when(boardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(board));
        when(listRepository.findByBoardIdAndIsDeletedFalseOrderByPositionAsc(1L)).thenReturn(new ArrayList<>());
        when(listRepository.save(any(ListEntity.class))).thenReturn(list);

        ListDTO result = listService.createList(request);

        assertNotNull(result);
        assertEquals("To Do", result.getName());
        verify(listRepository, times(1)).save(any(ListEntity.class));
    }

    @Test
    void testCreateList_WithPosition() {
        CreateListRequest request = new CreateListRequest();
        request.setName("New List");
        request.setBoardId(1L);
        request.setPosition(2);

        doNothing().when(permissionService).verifyBoardAccess(1L);
        when(boardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(board));
        when(listRepository.save(any(ListEntity.class))).thenReturn(list);

        ListDTO result = listService.createList(request);

        assertNotNull(result);
        verify(listRepository, times(1)).save(any(ListEntity.class));
    }

    @Test
    void testGetListById_Success() {
        when(listRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(list));

        ListDTO result = listService.getListById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("To Do", result.getName());
    }

    @Test
    void testGetListById_NotFound() {
        when(listRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> listService.getListById(1L));
    }

    @Test
    void testGetListsByBoardId_Success() {
        List<ListEntity> lists = Arrays.asList(list);

        when(listRepository.findByBoardIdAndIsDeletedFalseOrderByPositionAsc(1L)).thenReturn(lists);

        List<ListDTO> result = listService.getListsByBoardId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("To Do", result.get(0).getName());
    }

    @Test
    void testUpdateList_Success() {
        CreateListRequest request = new CreateListRequest();
        request.setName("Updated List Name");
        request.setPosition(1);

        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.canEditList(1L, testUser)).thenReturn(true);
        when(listRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(list));
        when(listRepository.save(any(ListEntity.class))).thenReturn(list);

        ListDTO result = listService.updateList(1L, request);

        assertNotNull(result);
        verify(listRepository, times(1)).save(any(ListEntity.class));
    }

    @Test
    void testUpdateList_AccessDenied() {
        CreateListRequest request = new CreateListRequest();
        request.setName("Updated List Name");

        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.canEditList(1L, testUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> listService.updateList(1L, request));
        verify(listRepository, never()).save(any(ListEntity.class));
    }

    @Test
    void testDeleteList_Success() {
        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.canDeleteList(1L, testUser)).thenReturn(true);
        when(listRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(list));
        when(listRepository.save(any(ListEntity.class))).thenReturn(list);

        assertDoesNotThrow(() -> listService.deleteList(1L));
        verify(listRepository, times(1)).save(any(ListEntity.class));
        assertTrue(list.getIsDeleted());
    }

    @Test
    void testDeleteList_AccessDenied() {
        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.canDeleteList(1L, testUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> listService.deleteList(1L));
        verify(listRepository, never()).save(any(ListEntity.class));
    }

    @Test
    void testMoveList_Success() {
        MoveListRequest request = new MoveListRequest();
        request.setNewPosition(2);

        ListEntity list2 = ListEntity.builder()
                .id(2L)
                .name("In Progress")
                .board(board)
                .position(1)
                .isDeleted(false)
                .build();

        ListEntity list3 = ListEntity.builder()
                .id(3L)
                .name("Done")
                .board(board)
                .position(2)
                .isDeleted(false)
                .build();

        List<ListEntity> allLists = new ArrayList<>(Arrays.asList(list, list2, list3));

        when(listRepository.findByIdWithBoard(1L)).thenReturn(Optional.of(list));
        when(boardRepository.findByIdWithWorkspace(1L)).thenReturn(Optional.of(board));
        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.isWorkspaceOwnerOrAdmin(1L, testUser)).thenReturn(true);
        when(listRepository.findByBoardIdAndIsDeletedFalseOrderByPositionAsc(1L)).thenReturn(allLists);
        when(listRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(listRepository.findByIdWithCards(1L)).thenReturn(Optional.of(list));

        ListDTO result = listService.moveList(1L, request);

        assertNotNull(result);
        verify(listRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testMoveList_AccessDenied() {
        MoveListRequest request = new MoveListRequest();
        request.setNewPosition(2);

        when(listRepository.findByIdWithBoard(1L)).thenReturn(Optional.of(list));
        when(boardRepository.findByIdWithWorkspace(1L)).thenReturn(Optional.of(board));
        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.isWorkspaceOwnerOrAdmin(1L, testUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> listService.moveList(1L, request));
        verify(listRepository, never()).saveAll(anyList());
    }
}


