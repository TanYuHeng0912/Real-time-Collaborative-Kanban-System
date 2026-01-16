import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { CardUpdateMessage } from '@/types';

interface UseWebSocketProps {
  boardId: number | null;
  onCardUpdate: (message: CardUpdateMessage) => void;
}

export const useWebSocket = ({ boardId, onCardUpdate }: UseWebSocketProps) => {
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!boardId) return;

    // Determine WebSocket URL - use environment variable or fallback to Render URL in production
    // VITE_WS_URL should be the full WebSocket URL including /ws endpoint
    // Example: wss://kanban-backend-d0s2.onrender.com/api/ws
    const wsUrl = import.meta.env.VITE_WS_URL || 
      (import.meta.env.PROD ? 'wss://kanban-backend-d0s2.onrender.com/api/ws' : '/api/ws');

    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log(`[WebSocket] Connected to board ${boardId}`);
        client.subscribe(`/topic/board/${boardId}`, (message) => {
          try {
            const update: CardUpdateMessage = JSON.parse(message.body);
            console.log('[WebSocket] Received update:', update);
            onCardUpdate(update);
          } catch (error) {
            console.error('[WebSocket] Error parsing message:', error, message.body);
          }
        });
      },
      onStompError: (frame) => {
        console.error('[WebSocket] STOMP error:', frame);
      },
      onWebSocketError: (event) => {
        console.error('[WebSocket] Connection error:', event);
      },
      onDisconnect: () => {
        console.log('[WebSocket] Disconnected');
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [boardId, onCardUpdate]);
};

