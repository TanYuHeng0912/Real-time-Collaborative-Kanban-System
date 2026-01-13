package com.kanban.repository;

import com.kanban.model.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListRepository extends JpaRepository<ListEntity, Long> {
    Optional<ListEntity> findByIdAndIsDeletedFalse(Long id);
    List<ListEntity> findByBoardIdAndIsDeletedFalseOrderByPositionAsc(Long boardId);
    
    @Query("SELECT l FROM ListEntity l WHERE l.id = :id AND l.isDeleted = false")
    Optional<ListEntity> findByIdWithCards(@Param("id") Long id);
}

