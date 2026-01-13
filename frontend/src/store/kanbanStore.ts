import { create } from 'zustand';
import { CardDTO, ListDTO, BoardDTO } from '@/types';

interface KanbanState {
  currentBoard: BoardDTO | null;
  setCurrentBoard: (board: BoardDTO | null) => void;
  updateCardOptimistic: (card: CardDTO) => void;
  moveCardOptimistic: (cardId: number, targetListId: number, newPosition: number) => void;
  deleteCardOptimistic: (cardId: number) => void;
  addCardOptimistic: (card: CardDTO) => void;
  rollbackBoard: (board: BoardDTO) => void;
  previousBoardState: BoardDTO | null;
}

export const useKanbanStore = create<KanbanState>((set, get) => ({
  currentBoard: null,
  previousBoardState: null,
  setCurrentBoard: (board) => set({ currentBoard: board, previousBoardState: board }),
  
  updateCardOptimistic: (card) => {
    const board = get().currentBoard;
    if (!board) return;
    
    set({ previousBoardState: JSON.parse(JSON.stringify(board)) });
    
    const updatedLists = board.lists.map(list => ({
      ...list,
      cards: list.cards.map(c => c.id === card.id ? card : c),
    }));
    
    set({ currentBoard: { ...board, lists: updatedLists } });
  },
  
  moveCardOptimistic: (cardId, targetListId, newPosition) => {
    const board = get().currentBoard;
    if (!board) return;
    
    set({ previousBoardState: JSON.parse(JSON.stringify(board)) });
    
    let cardToMove: CardDTO | null = null;
    
    // Remove card from source list
    const updatedLists = board.lists.map(list => {
      const cardIndex = list.cards.findIndex(c => c.id === cardId);
      if (cardIndex !== -1) {
        cardToMove = list.cards[cardIndex];
        return {
          ...list,
          cards: list.cards.filter(c => c.id !== cardId),
        };
      }
      return list;
    });
    
    // Add card to target list
    if (cardToMove) {
      const targetListIndex = updatedLists.findIndex(l => l.id === targetListId);
      if (targetListIndex !== -1) {
        const targetList = updatedLists[targetListIndex];
        const newCard = { ...cardToMove, listId: targetListId, position: newPosition };
        const updatedCards = [...targetList.cards, newCard].sort((a, b) => a.position - b.position);
        updatedLists[targetListIndex] = {
          ...targetList,
          cards: updatedCards,
        };
      }
    }
    
    set({ currentBoard: { ...board, lists: updatedLists } });
  },
  
  deleteCardOptimistic: (cardId) => {
    const board = get().currentBoard;
    if (!board) return;
    
    set({ previousBoardState: JSON.parse(JSON.stringify(board)) });
    
    const updatedLists = board.lists.map(list => ({
      ...list,
      cards: list.cards.filter(c => c.id !== cardId),
    }));
    
    set({ currentBoard: { ...board, lists: updatedLists } });
  },
  
  addCardOptimistic: (card) => {
    const board = get().currentBoard;
    if (!board) return;
    
    set({ previousBoardState: JSON.parse(JSON.stringify(board)) });
    
    const updatedLists = board.lists.map(list => {
      if (list.id === card.listId) {
        return {
          ...list,
          cards: [...list.cards, card].sort((a, b) => a.position - b.position),
        };
      }
      return list;
    });
    
    set({ currentBoard: { ...board, lists: updatedLists } });
  },
  
  rollbackBoard: (board) => {
    set({ currentBoard: board, previousBoardState: null });
  },
}));

