package com.kanban.repository;

import com.kanban.model.Board;
import com.kanban.model.Card;
import com.kanban.model.ListEntity;
import com.kanban.model.User;
import com.kanban.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CardRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CardRepository cardRepository;

    private User user;
    private Workspace workspace;
    private Board board;
    private ListEntity list;
    private Card card;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .passwordHash("hashed")
                .role(User.UserRole.USER)
                .isDeleted(false)
                .build();
        entityManager.persistAndFlush(user);

        workspace = Workspace.builder()
                .name("Test Workspace")
                .owner(user)
                .isDeleted(false)
                .build();
        entityManager.persistAndFlush(workspace);

        board = Board.builder()
                .name("Test Board")
                .workspace(workspace)
                .createdBy(user)
                .isDeleted(false)
                .build();
        entityManager.persistAndFlush(board);

        list = ListEntity.builder()
                .name("To Do")
                .board(board)
                .position(0)
                .isDeleted(false)
                .build();
        entityManager.persistAndFlush(list);

        card = Card.builder()
                .title("Test Card")
                .description("Test Description")
                .list(list)
                .position(0)
                .createdBy(user)
                .lastModifiedBy(user)
                .priority(Card.Priority.MEDIUM)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(card);
    }

    @Test
    void testFindByIdAndIsDeletedFalse_ExistingCard() {
        Optional<Card> found = cardRepository.findByIdAndIsDeletedFalse(card.getId());

        assertTrue(found.isPresent());
        assertEquals("Test Card", found.get().getTitle());
        assertFalse(found.get().getIsDeleted());
    }

    @Test
    void testFindByIdAndIsDeletedFalse_DeletedCard() {
        card.setIsDeleted(true);
        entityManager.persistAndFlush(card);

        Optional<Card> found = cardRepository.findByIdAndIsDeletedFalse(card.getId());

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByIdAndIsDeletedFalse_NonExistentCard() {
        Optional<Card> found = cardRepository.findByIdAndIsDeletedFalse(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByListIdAndIsDeletedFalseOrderByPositionAsc() {
        // Create additional cards
        Card card2 = Card.builder()
                .title("Card 2")
                .list(list)
                .position(1)
                .createdBy(user)
                .lastModifiedBy(user)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(card2);

        Card card3 = Card.builder()
                .title("Card 3")
                .list(list)
                .position(2)
                .createdBy(user)
                .lastModifiedBy(user)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(card3);

        // Create deleted card (should not be returned)
        Card deletedCard = Card.builder()
                .title("Deleted Card")
                .list(list)
                .position(3)
                .createdBy(user)
                .lastModifiedBy(user)
                .isDeleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(deletedCard);

        List<Card> cards = cardRepository.findByListIdAndIsDeletedFalseOrderByPositionAsc(list.getId());

        assertEquals(3, cards.size());
        assertEquals("Test Card", cards.get(0).getTitle());
        assertEquals("Card 2", cards.get(1).getTitle());
        assertEquals("Card 3", cards.get(2).getTitle());
        assertTrue(cards.stream().noneMatch(Card::getIsDeleted));
    }

    @Test
    void testFindMaxPositionByListId() {
        // Create cards with different positions
        Card card2 = Card.builder()
                .title("Card 2")
                .list(list)
                .position(5)
                .createdBy(user)
                .lastModifiedBy(user)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(card2);

        Integer maxPosition = cardRepository.findMaxPositionByListId(list.getId());

        assertEquals(5, maxPosition);
    }

    @Test
    void testFindMaxPositionByListId_NoCards() {
        // Create another list with no cards
        ListEntity emptyList = ListEntity.builder()
                .name("Empty List")
                .board(board)
                .position(1)
                .isDeleted(false)
                .build();
        entityManager.persistAndFlush(emptyList);

        Integer maxPosition = cardRepository.findMaxPositionByListId(emptyList.getId());

        assertNull(maxPosition);
    }

    @Test
    void testFindListIdByCardId() {
        Optional<Long> listId = cardRepository.findListIdByCardId(card.getId());

        assertTrue(listId.isPresent());
        assertEquals(list.getId(), listId.get());
    }

    @Test
    void testFindListIdByCardId_DeletedCard() {
        card.setIsDeleted(true);
        entityManager.persistAndFlush(card);

        Optional<Long> listId = cardRepository.findListIdByCardId(card.getId());

        assertFalse(listId.isPresent());
    }
}


