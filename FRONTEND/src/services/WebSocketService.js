import { Client } from '@stomp/stompjs';

class WebSocketService {
    // Initialize service with default values
    constructor() {
        this.client = null;
        this.brokerURL = 'ws://localhost:8080/ws/websocket';
        this._isConnected = false;
        this.subscriptions = new Set();
    }

    // Establish WebSocket connection with STOMP
    connect(onConnected, onError) {
        this.client = new Client({
            brokerURL: this.brokerURL,
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
                this._isConnected = true;
                console.log('WebSocket Connected');
                this.subscribeToUpdates();
                this.requestCurrentState();
                if (onConnected) onConnected();
            },
            onDisconnect: () => {
                this._isConnected = false;
                console.log('WebSocket Disconnected');
            },
            onStompError: (frame) => {
                console.error('Stomp error:', frame);
                if (onError) onError(frame);
            },
            onWebSocketError: (error) => {
                console.error('WebSocket error:', error);
                if (onError) onError(error);
            },
        });

        this.client.activate();
    }

    // Clean up subscriptions and disconnect
    disconnect() {
        if (this.client) {
            this.subscriptions.forEach(subscription => subscription.unsubscribe());
            this.subscriptions.clear();
            this.client.deactivate();
            this._isConnected = false;
        }
    }

    // Request current system state
    requestCurrentState() {
        if (this._isConnected && this.client) {
            this.client.publish({
                destination: '/app/system/state',
                body: JSON.stringify({})
            });
        }
    }

    // Load system settings
    loadSettings() {
        if (this._isConnected && this.client) {
            console.log('Loading settings');
            this.client.publish({
                destination: '/app/system/state',
                body: JSON.stringify({})
            });
        }
    }

    // Set handlers for different types of updates
    setTicketUpdateHandler(callback) {
        this.onTicketUpdate = callback;
    }

    setSystemStatusHandler(callback) {
        this.onSystemStatusUpdate = callback;
    }

    setSettingsUpdateHandler(callback) {
        this.onSettingsUpdate = callback;
    }

    // Subscribe to all relevant STOMP topics
    subscribeToUpdates() {
        // Subscribe to system status updates
        const statusSubscription = this.client.subscribe(
            '/topic/system-status',
            (message) => {
                try {
                    const data = JSON.parse(message.body);
                    console.log('Received system status:', data);
                    if (this.onSystemStatusUpdate) {
                        this.onSystemStatusUpdate(data);
                    }
                } catch (error) {
                    console.error('Error parsing system status:', error);
                }
            }
        );
        this.subscriptions.add(statusSubscription);

        // Subscribe to ticket updates
        const updateSubscription = this.client.subscribe(
            '/topic/ticket-updates',
            (message) => {
                try {
                    const data = JSON.parse(message.body);
                    console.log('Received ticket update:', data);
                    if (this.onTicketUpdate) {
                        this.onTicketUpdate(data);
                    }
                } catch (error) {
                    console.error('Error parsing ticket update:', error);
                }
            }
        );
        this.subscriptions.add(updateSubscription);

        // Subscribe to settings updates
        const settingsSubscription = this.client.subscribe(
            '/topic/settings',
            (message) => {
                try {
                    const data = JSON.parse(message.body);
                    console.log('Received settings update:', data);
                    if (this.onSettingsUpdate) {
                        this.onSettingsUpdate(data);
                    }
                } catch (error) {
                    console.error('Error parsing settings:', error);
                }
            }
        );
        this.subscriptions.add(settingsSubscription);
    }

    // Start vendor process
    startVendor(configuration) {
        if (!this._isConnected) {
            console.error('WebSocket not connected');
            return;
        }

        const payload = {
            totalTickets: configuration.totalTickets,
            releaseInterval: configuration.releaseInterval,
            purchaseInterval: configuration.purchaseInterval || 2,
            ticketsPerRelease: 1,
            maxTicketCapacity: configuration.maxTicketCapacity || 100,
            numVendors: 1,
            numCustomers: 0
        };

        console.log('Sending vendor start payload:', payload);

        this.client.publish({
            destination: '/app/system/start',
            body: JSON.stringify(payload)
        });
    }

    // Start customer process
    startCustomer(configuration) {
        if (!this._isConnected) {
            console.error('WebSocket not connected');
            return;
        }

        const payload = {
            totalTickets: configuration.totalTickets,
            releaseInterval: configuration.releaseInterval,
            purchaseInterval: configuration.purchaseInterval,
            ticketsPerRelease: 1,
            maxTicketCapacity: configuration.maxTicketCapacity || 100,
            numVendors: 0,
            numCustomers: 1
        };

        console.log('Sending customer start payload:', payload);

        this.client.publish({
            destination: '/app/customer/start',
            body: JSON.stringify(payload)
        });
    }

    // Save system settings
    saveSettings(configuration) {
        if (!this._isConnected) {
            console.error('WebSocket not connected');
            return;
        }

        const payload = {
            totalTickets: configuration.maxTicketCapacity,
            releaseInterval: configuration.releaseInterval,
            purchaseInterval: configuration.purchaseInterval,
            ticketsPerRelease: configuration.ticketsPerRelease,
            maxTicketCapacity: configuration.maxTicketCapacity,
            numVendors: 0,
            numCustomers: 0
        };

        console.log('Saving settings:', payload);
        this.client.publish({
            destination: '/app/settings/save',
            body: JSON.stringify(payload)
        });
    }
}

export default WebSocketService;