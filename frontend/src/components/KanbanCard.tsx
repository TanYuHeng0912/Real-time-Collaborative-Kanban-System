import { Draggable } from '@hello-pangea/dnd';
import { CardDTO } from '@/types';

interface KanbanCardProps {
  card: CardDTO;
  index: number;
}

export default function KanbanCard({ card, index }: KanbanCardProps) {
  return (
    <Draggable draggableId={card.id.toString()} index={index}>
      {(provided, snapshot) => (
        <div
          ref={provided.innerRef}
          {...provided.draggableProps}
          {...provided.dragHandleProps}
          className={`bg-white p-3 rounded border border-gray-200 cursor-move hover:shadow-md transition-shadow ${
            snapshot.isDragging ? 'shadow-lg ring-2 ring-blue-500' : 'shadow-sm'
          }`}
        >
          <h3 className="font-medium text-sm text-gray-900 mb-1">{card.title}</h3>
          {card.description && (
            <p className="text-xs text-gray-600 line-clamp-2 mt-1">{card.description}</p>
          )}
          {card.dueDate && (
            <div className="mt-2 text-xs text-gray-500">
              Due: {new Date(card.dueDate).toLocaleDateString()}
            </div>
          )}
        </div>
      )}
    </Draggable>
  );
}

