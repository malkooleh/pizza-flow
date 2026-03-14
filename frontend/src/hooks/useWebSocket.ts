import { Client, type IMessage, type IFrame } from '@stomp/stompjs';
import { useEffect, useRef, useState } from 'react';

export const useWebSocket = (topic: string, onMessage: (data: any) => void) => {
    const clientRef = useRef<Client | null>(null);
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        const client = new Client({
            brokerURL: 'ws://localhost:8080/ws/kitchen', // Gateway WS proxy to kitchen-service
            onConnect: () => {
                setIsConnected(true);
                console.log('Connected to WebSocket');

                client.subscribe(topic, (message: IMessage) => {
                    if (message.body) {
                        onMessage(JSON.parse(message.body));
                    }
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
                console.log('Disconnected from WebSocket');
            },
            onStompError: (frame: IFrame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            },
        });

        client.activate();
        clientRef.current = client;

        return () => {
            client.deactivate();
        };
    }, [topic]);

    return { isConnected };
};
