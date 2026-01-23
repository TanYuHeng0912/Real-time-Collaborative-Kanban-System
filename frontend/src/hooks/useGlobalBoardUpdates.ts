import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BoardUpdateMessage } from '@/types';

interface UseGlobalBoardUpdatesProps {
  onBoardUpdate: (message: BoardUpdateMessage) => void;
}

export const useGlobalBoardUpdates = ({ onBoardUpdate }: UseGlobalBoardUpdatesProps) => {
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const wsUrl = import.meta.env.VITE_WS_URL || '/api/ws';
    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl) as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        client.subscribe(`/topic/boards`, (message) => {
          const update: BoardUpdateMessage = JSON.parse(message.body);
          onBoardUpdate(update);
        });
      },
      onStompError: (frame) => {
        console.error('WebSocket error:', frame);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [onBoardUpdate]);
};

