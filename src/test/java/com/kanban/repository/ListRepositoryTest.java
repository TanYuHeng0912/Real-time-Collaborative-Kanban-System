package com.kanban.repository;

import com.kanban.model.Board;
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
class ListRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ListRepository listRepository;

    private User user;
    private Workspace workspace;
    private Board board;
    private ListEntity list;

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
    }

    @Test
    void testFindByIdAndIsDeletedFalse_ExistingList() {
        Optional<ListEntity> found = listRepository.findByIdAndIsDeletedFalse(list.getId());

        assertTrue(found.isPresent());
        assertEquals("To Do", found.get().getName());
        assertFalse(found.get().getIsDeleted());
    }

    @Test
    void testFindByIdAndIsDeletedFalse_DeletedList() {
        list.setIsDeleted(true);
        entityManager.persistAndFlush(list);

        Optional<ListEntity> found = listRepository.findByIdAndIsDeletedFalse(list.getId());

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByBoardIdAndIsDeletedFalseOrderByPositionAsc() {
        // Create additional lists
        ListEntity list2 = ListEntity.builder()
                .name("In Progress")
                .board(board)
                .position(1)
                .isDeleted(false)
                .build();
        entityManager.persistAndFlush(list2);

        ListEntity list3 = ListEntity.builder()
                .name("Done")
                .board(board)
                .position(2)
                .isDeleted(false)
                .build();
        entityManager.persistAndFlush(list3);

        // Create deleted list (should not be returned)
        ListEntity deletedList = ListEntity.builder()
                .name("Deleted List")
                .board(board)
                .position(3)
                .isDeleted(true)
                .build();
        entityManager.persistAndFlush(deletedList);

        List<ListEntity> lists = listRepository.findByBoardIdAndIsDeletedFalseOrderByPositionAsc(board.getId());

        assertEquals(3, lists.size());
        assertEquals("To Do", lists.get(0).getName());
        assertEquals("In Progress", lists.get(1).getName());
        assertEquals("Done", lists.get(2).getName());
        assertTrue(lists.stream().noneMatch(ListEntity::getIsDeleted));
    }

    @Test
    void testFindByIdWithBoard() {
        Optional<ListEntity> found = listRepository.findByIdWithBoard(list.getId());

        assertTrue(found.isPresent());
        assertNotNull(found.get().getBoard());
        assertEquals(board.getId(), found.get().getBoard().getId());
    }

    @Test
    void testFindByIdWithCards() {
        Optional<ListEntity> found = listRepository.findByIdWithCards(list.getId());

        assertTrue(found.isPresent());
        // Cards might be empty, but the list should be found
        assertNotNull(found.get().getCards());
    }
}


