import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { boardService } from '@/services/boardService';
import { useKanbanStore } from '@/store/kanbanStore';
import { useWebSocket } from '@/hooks/useWebSocket';
import { CardUpdateMessage } from '@/types';
import KanbanBoard from '@/components/KanbanBoard';
import CreateBoardDialog from '@/components/CreateBoardDialog';
import Navigation from '@/components/Navigation';

export default function DashboardPage() {
  const { boardId } = useParams<{ boardId: string }>();
  const navigate = useNavigate();
  const { setCurrentBoard, currentBoard, updateCardOptimistic, moveCardOptimistic, deleteCardOptimistic, addCardOptimistic } = useKanbanStore();
  
  const { data: board, isLoading, error } = useQuery({
    queryKey: ['board', boardId],
    queryFn: () => boardService.getBoardById(Number(boardId)),
    enabled: !!boardId,
    retry: false,
  });

  useEffect(() => {
    if (board) {
      setCurrentBoard(board);
    }
  }, [board, setCurrentBoard]);

  const handleCardUpdate = (message: CardUpdateMessage) => {
    if (!currentBoard || message.boardId !== currentBoard.id) return;

    switch (message.type) {
      case 'CREATED':
        if (message.card) {
          addCardOptimistic(message.card);
        }
        break;
      case 'UPDATED':
        if (message.card) {
          updateCardOptimistic(message.card);
        }
        break;
      case 'MOVED':
        if (message.card) {
          moveCardOptimistic(message.card.id, message.card.listId, message.card.position);
        }
        break;
      case 'DELETED':
        if (message.cardId) {
          deleteCardOptimistic(message.cardId);
        }
        break;
    }
  };

  const handleBoardCreated = (newBoardId: number) => {
    navigate(`/dashboard/${newBoardId}`);
  };

  useWebSocket({
    boardId: boardId ? Number(boardId) : null,
    onCardUpdate: handleCardUpdate,
  });

  // If no boardId, show create board dialog
  if (!boardId) {
    return (
      <div className="h-screen bg-gray-50 flex flex-col">
        <Navigation />
        <div className="flex-1 flex items-center justify-center">
          <CreateBoardDialog onBoardCreated={handleBoardCreated} />
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="h-screen bg-gray-50 flex flex-col">
        <Navigation />
        <div className="flex-1 flex items-center justify-center">Loading...</div>
      </div>
    );
  }

  if ((!board && !isLoading) || error) {
    return (
      <div className="h-screen bg-gray-50 flex flex-col">
        <Navigation />
        <div className="flex-1 flex items-center justify-center">
          <CreateBoardDialog onBoardCreated={handleBoardCreated} />
        </div>
      </div>
    );
  }

  if (!currentBoard) {
    return <div className="flex items-center justify-center h-screen">Loading...</div>;
  }

  return (
    <div className="h-screen bg-gray-50 flex flex-col">
      <Navigation />
      <div className="flex-1 flex flex-col overflow-hidden">
        <div className="bg-white border-b border-gray-200 px-6 py-4">
          <h1 className="text-2xl font-semibold text-gray-900">{currentBoard.name}</h1>
          {currentBoard.description && (
            <p className="text-gray-600 mt-1 text-sm">{currentBoard.description}</p>
          )}
        </div>
        <div className="flex-1 overflow-hidden">
          <KanbanBoard board={currentBoard} />
        </div>
      </div>
    </div>
  );
}

