package com.kanban.service;

import com.kanban.model.Board;
import com.kanban.model.Card;
import com.kanban.model.ListEntity;
import com.kanban.model.User;
import com.kanban.model.Workspace;
import com.kanban.model.WorkspaceMember;
import com.kanban.repository.BoardMemberRepository;
import com.kanban.repository.BoardRepository;
import com.kanban.repository.CardRepository;
import com.kanban.repository.ListRepository;
import com.kanban.repository.UserRepository;
import com.kanban.repository.WorkspaceMemberRepository;
import com.kanban.repository.WorkspaceRepository;
import com.kanban.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ListRepository listRepository;

    @InjectMocks
    private PermissionService permissionService;

    private User adminUser;
    private User regularUser;
    private User workspaceOwner;
    private Workspace workspace;
    private Board board;
    private Card card;
    private ListEntity list;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .fullName("Admin User")
                .role(User.UserRole.ADMIN)
                .isDeleted(false)
                .build();

        regularUser = User.builder()
                .id(2L)
                .username("regular")
                .email("regular@example.com")
                .fullName("Regular User")
                .role(User.UserRole.USER)
                .isDeleted(false)
                .build();

        workspaceOwner = User.builder()
                .id(3L)
                .username("owner")
                .email("owner@example.com")
                .fullName("Workspace Owner")
                .role(User.UserRole.USER)
                .isDeleted(false)
                .build();

        workspace = Workspace.builder()
                .id(1L)
                .name("Test Workspace")
                .owner(workspaceOwner)
                .isDeleted(false)
                .build();

        board = Board.builder()
                .id(1L)
                .name("Test Board")
                .workspace(workspace)
                .createdBy(workspaceOwner)
                .isDeleted(false)
                .build();

        list = ListEntity.builder()
                .id(1L)
                .name("To Do")
                .board(board)
                .position(0)
                .isDeleted(false)
                .build();

        card = Card.builder()
                .id(1L)
                .title("Test Card")
                .description("Test Description")
                .list(list)
                .position(0)
                .createdBy(workspaceOwner)
                .assignedUsers(new ArrayList<>())
                .priority(Card.Priority.MEDIUM)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetCurrentUser_Success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("regular");
            when(userRepository.findByUsernameAndIsDeletedFalse("regular")).thenReturn(Optional.of(regularUser));

            User result = permissionService.getCurrentUser();

            assertNotNull(result);
            assertEquals("regular", result.getUsername());
        }
    }

    @Test
    void testGetCurrentUser_NotFound() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("nonexistent");
            when(userRepository.findByUsernameAndIsDeletedFalse("nonexistent")).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> permissionService.getCurrentUser());
        }
    }

    @Test
    void testHasWorkspaceAccess_Admin() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("admin");
            when(userRepository.findByUsernameAndIsDeletedFalse("admin")).thenReturn(Optional.of(adminUser));

            boolean result = permissionService.hasWorkspaceAccess(1L);

            assertTrue(result);
            verify(workspaceMemberRepository, never()).existsByWorkspaceIdAndUserIdAndIsDeletedFalse(anyLong(), anyLong());
        }
    }

    @Test
    void testHasWorkspaceAccess_RegularUser() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("regular");
            when(userRepository.findByUsernameAndIsDeletedFalse("regular")).thenReturn(Optional.of(regularUser));
            when(workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndIsDeletedFalse(1L, 2L)).thenReturn(true);

            boolean result = permissionService.hasWorkspaceAccess(1L);

            assertTrue(result);
            verify(workspaceMemberRepository, times(1)).existsByWorkspaceIdAndUserIdAndIsDeletedFalse(1L, 2L);
        }
    }

    @Test
    void testHasBoardAccess_Admin() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("admin");
            when(userRepository.findByUsernameAndIsDeletedFalse("admin")).thenReturn(Optional.of(adminUser));

            boolean result = permissionService.hasBoardAccess(1L);

            assertTrue(result);
        }
    }

    @Test
    void testHasBoardAccess_BoardMember() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("regular");
            when(userRepository.findByUsernameAndIsDeletedFalse("regular")).thenReturn(Optional.of(regularUser));
            when(boardMemberRepository.existsByBoardIdAndUserIdAndIsDeletedFalse(1L, 2L)).thenReturn(true);

            boolean result = permissionService.hasBoardAccess(1L);

            assertTrue(result);
        }
    }

    @Test
    void testHasBoardAccess_WorkspaceMember() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("regular");
            when(userRepository.findByUsernameAndIsDeletedFalse("regular")).thenReturn(Optional.of(regularUser));
            when(boardMemberRepository.existsByBoardIdAndUserIdAndIsDeletedFalse(1L, 2L)).thenReturn(false);
            when(boardRepository.findByIdWithWorkspace(1L)).thenReturn(Optional.of(board));
            when(workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndIsDeletedFalse(1L, 2L)).thenReturn(true);

            boolean result = permissionService.hasBoardAccess(1L);

            assertTrue(result);
        }
    }

    @Test
    void testCanEditCard_Admin() {
        assertTrue(permissionService.canEditCard(1L, adminUser));
        verify(cardRepository, never()).findByIdAndIsDeletedFalse(anyLong());
    }

    @Test
    void testCanEditCard_Creator() {
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));

        boolean result = permissionService.canEditCard(1L, workspaceOwner);

        assertTrue(result);
    }

    @Test
    void testCanEditCard_AssignedUser() {
        card.getAssignedUsers().add(regularUser);
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));

        boolean result = permissionService.canEditCard(1L, regularUser);

        assertTrue(result);
    }

    @Test
    void testCanEditCard_NoPermission() {
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));

        boolean result = permissionService.canEditCard(1L, regularUser);

        assertFalse(result);
    }

    @Test
    void testCanDeleteCard_Admin() {
        assertTrue(permissionService.canDeleteCard(1L, adminUser));
        verify(cardRepository, never()).findByIdAndIsDeletedFalse(anyLong());
    }

    @Test
    void testCanDeleteCard_Creator() {
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));

        boolean result = permissionService.canDeleteCard(1L, workspaceOwner);

        assertTrue(result);
    }

    @Test
    void testCanEditList_Admin() {
        assertTrue(permissionService.canEditList(1L, adminUser));
        verify(listRepository, never()).findByIdWithBoard(anyLong());
    }

    @Test
    void testCanEditList_WorkspaceOwner() {
        when(listRepository.findByIdWithBoard(1L)).thenReturn(Optional.of(list));
        when(workspaceRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(workspace));

        boolean result = permissionService.canEditList(1L, workspaceOwner);

        assertTrue(result); // Workspace owner can edit
    }

    @Test
    void testCanEditList_WorkspaceAdmin() {
        WorkspaceMember adminMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(regularUser)
                .role(WorkspaceMember.WorkspaceRole.ADMIN)
                .isDeleted(false)
                .build();

        when(listRepository.findByIdWithBoard(1L)).thenReturn(Optional.of(list));
        when(workspaceRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(workspace));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserIdAndIsDeletedFalse(1L, 2L))
                .thenReturn(Optional.of(adminMember));

        boolean result = permissionService.canEditList(1L, regularUser);

        assertTrue(result);
    }

    @Test
    void testIsWorkspaceOwnerOrAdmin_Admin() {
        assertTrue(permissionService.isWorkspaceOwnerOrAdmin(1L, adminUser));
        verify(workspaceRepository, never()).findByIdAndIsDeletedFalse(anyLong());
    }

    @Test
    void testIsWorkspaceOwnerOrAdmin_Owner() {
        when(workspaceRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(workspace));

        boolean result = permissionService.isWorkspaceOwnerOrAdmin(1L, workspaceOwner);

        assertTrue(result);
    }

    @Test
    void testVerifyWorkspaceAccess_Allowed() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("regular");
            when(userRepository.findByUsernameAndIsDeletedFalse("regular")).thenReturn(Optional.of(regularUser));
            when(workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndIsDeletedFalse(1L, 2L)).thenReturn(true);

            assertDoesNotThrow(() -> permissionService.verifyWorkspaceAccess(1L));
        }
    }

    @Test
    void testVerifyWorkspaceAccess_Denied() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("regular");
            when(userRepository.findByUsernameAndIsDeletedFalse("regular")).thenReturn(Optional.of(regularUser));
            when(workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndIsDeletedFalse(1L, 2L)).thenReturn(false);

            assertThrows(RuntimeException.class, () -> permissionService.verifyWorkspaceAccess(1L));
        }
    }

    @Test
    void testVerifyAdmin_Admin() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("admin");
            when(userRepository.findByUsernameAndIsDeletedFalse("admin")).thenReturn(Optional.of(adminUser));

            assertDoesNotThrow(() -> permissionService.verifyAdmin());
        }
    }

    @Test
    void testVerifyAdmin_NotAdmin() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("regular");
            when(userRepository.findByUsernameAndIsDeletedFalse("regular")).thenReturn(Optional.of(regularUser));

            assertThrows(RuntimeException.class, () -> permissionService.verifyAdmin());
        }
    }
}


