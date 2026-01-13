package com.kanban.repository;

import com.kanban.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByIdAndIsDeletedFalse(Long id);
    List<Board> findByWorkspaceIdAndIsDeletedFalse(Long workspaceId);
    
    @Query("SELECT b FROM Board b WHERE b.id = :id AND b.isDeleted = false")
    Optional<Board> findByIdWithLists(@Param("id") Long id);
}

