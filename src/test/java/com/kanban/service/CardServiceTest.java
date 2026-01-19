package com.kanban.service;

import com.kanban.dto.CardDTO;
import com.kanban.dto.CreateCardRequest;
import com.kanban.dto.MoveCardRequest;
import com.kanban.dto.UpdateCardRequest;
import com.kanban.model.Board;
import com.kanban.model.Card;
import com.kanban.model.ListEntity;
import com.kanban.model.User;
import com.kanban.model.Workspace;
import com.kanban.repository.CardRepository;
import com.kanban.repository.ListRepository;
import com.kanban.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ListRepository listRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private User assignedUser;
    private Workspace workspace;
    private Board board;
    private ListEntity list;
    private Card card;

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

        assignedUser = User.builder()
                .id(2L)
                .username("assigneduser")
                .email("assigned@example.com")
                .fullName("Assigned User")
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
                .build();

        card = Card.builder()
                .id(1L)
                .title("Test Card")
                .description("Test Description")
                .list(list)
                .position(0)
                .createdBy(testUser)
                .assignedUsers(new ArrayList<>())
                .lastModifiedBy(testUser)
                .priority(Card.Priority.MEDIUM)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateCard_Success() {
        CreateCardRequest request = new CreateCardRequest();
        request.setTitle("New Card");
        request.setDescription("New Description");
        request.setListId(1L);
        request.setPosition(null); // Auto-calculate position

        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(listRepository.findByIdWithBoard(1L)).thenReturn(Optional.of(list));
        when(permissionService.hasBoardAccess(1L, testUser)).thenReturn(true);
        when(cardRepository.findMaxPositionByListId(1L)).thenReturn(0);
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardDTO result = cardService.createCard(request);

        assertNotNull(result);
        assertEquals("Test Card", result.getTitle());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void testCreateCard_WithAssignedUsers() {
        CreateCardRequest request = new CreateCardRequest();
        request.setTitle("New Card");
        request.setListId(1L);
        request.setAssignedUserIds(Arrays.asList(2L));

        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(listRepository.findByIdWithBoard(1L)).thenReturn(Optional.of(list));
        when(permissionService.hasBoardAccess(1L, testUser)).thenReturn(true);
        when(cardRepository.findMaxPositionByListId(1L)).thenReturn(null);
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignedUser));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card savedCard = invocation.getArgument(0);
            savedCard.setId(1L);
            return savedCard;
        });

        CardDTO result = cardService.createCard(request);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void testCreateCard_AccessDenied() {
        CreateCardRequest request = new CreateCardRequest();
        request.setTitle("New Card");
        request.setListId(1L);

        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(listRepository.findByIdWithBoard(1L)).thenReturn(Optional.of(list));
        when(permissionService.hasBoardAccess(1L, testUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> cardService.createCard(request));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testGetCardById_Success() {
        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));
        when(listRepository.findByIdWithBoard(1L)).thenReturn(Optional.of(list));
        when(permissionService.hasBoardAccess(1L, testUser)).thenReturn(true);

        CardDTO result = cardService.getCardById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Card", result.getTitle());
    }

    @Test
    void testGetCardById_NotFound() {
        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cardService.getCardById(1L));
    }

    @Test
    void testGetCardsByListId_Success() {
        List<Card> cards = Arrays.asList(card);

        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(listRepository.findByIdWithBoard(1L)).thenReturn(Optional.of(list));
        when(permissionService.hasBoardAccess(1L, testUser)).thenReturn(true);
        when(cardRepository.findByListIdAndIsDeletedFalseOrderByPositionAsc(1L)).thenReturn(cards);

        List<CardDTO> result = cardService.getCardsByListId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Card", result.get(0).getTitle());
    }

    @Test
    void testUpdateCard_Success() {
        UpdateCardRequest request = new UpdateCardRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");

        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.canEditCard(1L, testUser)).thenReturn(true);
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardDTO result = cardService.updateCard(1L, request);

        assertNotNull(result);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void testUpdateCard_AccessDenied() {
        UpdateCardRequest request = new UpdateCardRequest();
        request.setTitle("Updated Title");

        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.canEditCard(1L, testUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> cardService.updateCard(1L, request));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testUpdateCard_PriorityChange() {
        UpdateCardRequest request = new UpdateCardRequest();
        request.setPriority("HIGH");

        card.getAssignedUsers().add(testUser);

        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.canEditCard(1L, testUser)).thenReturn(true);
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardDTO result = cardService.updateCard(1L, request);

        assertNotNull(result);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void testMoveCard_Success() {
        MoveCardRequest request = new MoveCardRequest();
        request.setTargetListId(1L);
        request.setNewPosition(1);

        ListEntity targetList = ListEntity.builder()
                .id(1L)
                .name("Target List")
                .board(board)
                .position(1)
                .isDeleted(false)
                .build();

        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.canEditCard(1L, testUser)).thenReturn(true);
        when(listRepository.findByIdWithBoard(1L)).thenReturn(Optional.of(targetList));
        when(permissionService.hasBoardAccess(1L, testUser)).thenReturn(true);
        when(cardRepository.findListIdByCardId(1L)).thenReturn(Optional.of(1L));
        when(cardRepository.findByListIdAndIsDeletedFalseOrderByPositionAsc(anyLong())).thenReturn(new ArrayList<>());
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        CardDTO result = cardService.moveCard(1L, request);

        assertNotNull(result);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void testMoveCard_AccessDenied() {
        MoveCardRequest request = new MoveCardRequest();
        request.setTargetListId(1L);
        request.setNewPosition(1);

        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.canEditCard(1L, testUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> cardService.moveCard(1L, request));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testDeleteCard_Success() {
        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.canDeleteCard(1L, testUser)).thenReturn(true);
        when(cardRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        assertDoesNotThrow(() -> cardService.deleteCard(1L));
        verify(cardRepository, times(1)).save(any(Card.class));
        assertTrue(card.getIsDeleted());
    }

    @Test
    void testDeleteCard_AccessDenied() {
        when(permissionService.getCurrentUser()).thenReturn(testUser);
        when(permissionService.canDeleteCard(1L, testUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> cardService.deleteCard(1L));
        verify(cardRepository, never()).save(any(Card.class));
    }
}


