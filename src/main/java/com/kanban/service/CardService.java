package com.kanban.service;

import com.kanban.dto.CardDTO;
import com.kanban.dto.CreateCardRequest;
import com.kanban.dto.MoveCardRequest;
import com.kanban.dto.UpdateCardRequest;
import com.kanban.model.Card;
import com.kanban.model.ListEntity;
import com.kanban.model.User;
import com.kanban.repository.CardRepository;
import com.kanban.repository.ListRepository;
import com.kanban.repository.UserRepository;
import com.kanban.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {
    
    private final CardRepository cardRepository;
    private final ListRepository listRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public CardDTO createCard(CreateCardRequest request) {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ListEntity list = listRepository.findByIdAndIsDeletedFalse(request.getListId())
                .orElseThrow(() -> new RuntimeException("List not found"));
        
        Integer position = request.getPosition();
        if (position == null) {
            Integer maxPosition = cardRepository.findMaxPositionByListId(request.getListId());
            position = maxPosition == null ? 0 : maxPosition + 1;
        }
        
        User assignedTo = null;
        if (request.getAssignedTo() != null) {
            assignedTo = userRepository.findById(request.getAssignedTo())
                    .orElse(null);
        }
        
        Card card = Card.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .list(list)
                .position(position)
                .createdBy(user)
                .assignedTo(assignedTo)
                .dueDate(request.getDueDate())
                .isDeleted(false)
                .build();
        
        card = cardRepository.save(card);
        return toDTO(card);
    }
    
    @Transactional(readOnly = true)
    public CardDTO getCardById(Long id) {
        Card card = cardRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        return toDTO(card);
    }
    
    @Transactional(readOnly = true)
    public List<CardDTO> getCardsByListId(Long listId) {
        List<Card> cards = cardRepository.findByListIdAndIsDeletedFalseOrderByPositionAsc(listId);
        return cards.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CardDTO updateCard(Long id, UpdateCardRequest request) {
        Card card = cardRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (request.getTitle() != null) {
            card.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            card.setDescription(request.getDescription());
        }
        if (request.getPosition() != null) {
            card.setPosition(request.getPosition());
        }
        if (request.getAssignedTo() != null) {
            User assignedTo = userRepository.findById(request.getAssignedTo())
                    .orElse(null);
            card.setAssignedTo(assignedTo);
        }
        if (request.getDueDate() != null) {
            card.setDueDate(request.getDueDate());
        }
        
        card = cardRepository.save(card);
        return toDTO(card);
    }
    
    @Transactional
    public CardDTO moveCard(Long id, MoveCardRequest request) {
        Card card = cardRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        ListEntity targetList = listRepository.findByIdAndIsDeletedFalse(request.getTargetListId())
                .orElseThrow(() -> new RuntimeException("Target list not found"));
        
        card.setList(targetList);
        card.setPosition(request.getNewPosition());
        
        card = cardRepository.save(card);
        return toDTO(card);
    }
    
    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        card.setIsDeleted(true);
        cardRepository.save(card);
    }
    
    private CardDTO toDTO(Card card) {
        return CardDTO.builder()
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

