import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { boardService } from '@/services/boardService';
import { CardDTO } from '@/types';
import Navigation from '@/components/Navigation';
import { useEffect, useMemo } from 'react';

export default function GanttChartPage() {
  const { boardId } = useParams<{ boardId: string }>();
  const navigate = useNavigate();

  const { data: board, isLoading } = useQuery({
    queryKey: ['board', boardId],
    queryFn: () => boardService.getBoardById(Number(boardId)),
    enabled: !!boardId,
  });

  // Collect all cards with due dates
  const cardsWithDueDates = useMemo(() => {
    if (!board) return [];
    const cards: (CardDTO & { listName: string })[] = [];
    board.lists.forEach(list => {
      list.cards
        .filter(card => card.dueDate)
        .forEach(card => {
          cards.push({ ...card, listName: list.name });
        });
    });
    return cards.sort((a, b) => {
      const dateA = new Date(a.dueDate!).getTime();
      const dateB = new Date(b.dueDate!).getTime();
      return dateA - dateB;
    });
  }, [board]);

  // Calculate date range
  const dateRange = useMemo(() => {
    if (cardsWithDueDates.length === 0) return { min: new Date(), max: new Date() };
    
    const dates = cardsWithDueDates.map(card => new Date(card.dueDate!));
    const min = new Date(Math.min(...dates.map(d => d.getTime())));
    const max = new Date(Math.max(...dates.map(d => d.getTime())));
    
    // Add padding
    min.setDate(min.getDate() - 7);
    max.setDate(max.getDate() + 7);
    
    return { min, max };
  }, [cardsWithDueDates]);

  const daysInRange = useMemo(() => {
    const diffTime = Math.abs(dateRange.max.getTime() - dateRange.min.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  }, [dateRange]);

  const getPriorityColor = (priority?: string) => {
    switch (priority) {
      case 'HIGH':
        return '#ef4444'; // red-500
      case 'LOW':
        return '#3b82f6'; // blue-500
      case 'DONE':
        return '#22c55e'; // green-500
      case 'MEDIUM':
      default:
        return '#eab308'; // yellow-500
    }
  };

  const getPositionForDate = (date: Date) => {
    const diffTime = date.getTime() - dateRange.min.getTime();
    const diffDays = diffTime / (1000 * 60 * 60 * 24);
    return (diffDays / daysInRange) * 100;
  };

  const getWidthForCard = (card: CardDTO) => {
    // Default width is 5% of the range, but can be adjusted
    const baseWidth = 5;
    return Math.max(baseWidth, (1 / daysInRange) * 100);
  };

  // Redirect if no boardId
  useEffect(() => {
    if (!boardId) {
      navigate('/dashboard');
    }
  }, [boardId, navigate]);

  if (isLoading) {
    return (
      <div className="h-screen flex flex-col">
        <Navigation />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-gray-500">Loading...</div>
        </div>
      </div>
    );
  }

  if (!board) {
    return (
      <div className="h-screen flex flex-col">
        <Navigation />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-gray-500">Board not found</div>
        </div>
      </div>
    );
  }

  if (cardsWithDueDates.length === 0) {
    return (
      <div className="h-screen flex flex-col">
        <Navigation />
        <div className="flex-1 p-6">
          <div className="mb-4">
            <h1 className="text-2xl font-bold text-gray-900">{board.name} - Gantt Chart</h1>
            <p className="text-gray-500 mt-1">No cards with due dates found</p>
          </div>
          <button
            onClick={() => navigate(`/dashboard/${boardId}`)}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
          >
            Back to Board
          </button>
        </div>
      </div>
    );
  }

  // Generate date labels
  const dateLabels: Date[] = [];
  const currentDate = new Date(dateRange.min);
  while (currentDate <= dateRange.max) {
    dateLabels.push(new Date(currentDate));
    currentDate.setDate(currentDate.getDate() + 1);
  }

  const formatDate = (date: Date) => {
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  };

  return (
    <div className="h-screen flex flex-col bg-gray-50">
      <Navigation />
      <div className="flex-1 overflow-auto p-6">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{board.name} - Gantt Chart</h1>
            <p className="text-gray-500 mt-1">
              {cardsWithDueDates.length} card{cardsWithDueDates.length !== 1 ? 's' : ''} with due dates
            </p>
          </div>
          <button
            onClick={() => navigate(`/dashboard/${boardId}`)}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
          >
            Back to Board
          </button>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          {/* Date Header */}
          <div className="border-b border-gray-200 bg-gray-50">
            <div className="relative" style={{ minHeight: '60px' }}>
              {dateLabels.map((date, idx) => {
                const isWeekend = date.getDay() === 0 || date.getDay() === 6;
                return (
                  <div
                    key={idx}
                    className={`absolute top-0 bottom-0 border-r border-gray-300 text-xs text-center py-2 ${
                      isWeekend ? 'bg-gray-100' : 'bg-white'
                    }`}
                    style={{
                      left: `${(idx / (dateLabels.length - 1)) * 100}%`,
                      width: `${100 / dateLabels.length}%`,
                    }}
                  >
                    <div className="font-medium text-gray-700">{formatDate(date)}</div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Cards */}
          <div className="relative" style={{ minHeight: `${cardsWithDueDates.length * 60}px` }}>
            {cardsWithDueDates.map((card, idx) => {
              const dueDate = new Date(card.dueDate!);
              const position = getPositionForDate(dueDate);
              const width = getWidthForCard(card);
              const priorityColor = getPriorityColor(card.priority);

              return (
                <div
                  key={card.id}
                  className="absolute border-t border-gray-200"
                  style={{
                    top: `${idx * 60}px`,
                    height: '56px',
                    width: '100%',
                  }}
                >
                  <div className="h-full flex items-center px-4">
                    <div className="flex-1 mr-4">
                      <div className="font-medium text-sm text-gray-900 truncate">
                        {card.title}
                      </div>
                      <div className="text-xs text-gray-500">
                        {card.listName} • {card.assignedUserNames && card.assignedUserNames.length > 0 
                          ? card.assignedUserNames.join(', ') 
                          : 'Unassigned'}
                      </div>
                    </div>
                  </div>
                  
                  {/* Gantt Bar */}
                  <div
                    className="absolute top-2 rounded px-2 py-1 text-white text-xs font-medium shadow-sm"
                    style={{
                      left: `${Math.max(0, position - width / 2)}%`,
                      width: `${width}%`,
                      backgroundColor: priorityColor,
                      minWidth: '80px',
                    }}
                    title={`${card.title} - Due: ${formatDate(dueDate)}`}
                  >
                    <div className="truncate">{card.title}</div>
                    <div className="text-xs opacity-90">{formatDate(dueDate)}</div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Legend */}
        <div className="mt-4 bg-white rounded-lg shadow-sm border border-gray-200 p-4">
          <h3 className="text-sm font-medium text-gray-700 mb-3">Priority Legend</h3>
          <div className="flex gap-4">
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded" style={{ backgroundColor: '#ef4444' }}></div>
              <span className="text-sm text-gray-600">High Priority</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded" style={{ backgroundColor: '#eab308' }}></div>
              <span className="text-sm text-gray-600">Medium Priority</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded" style={{ backgroundColor: '#3b82f6' }}></div>
              <span className="text-sm text-gray-600">Low Priority</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 rounded" style={{ backgroundColor: '#22c55e' }}></div>
              <span className="text-sm text-gray-600">Done</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

