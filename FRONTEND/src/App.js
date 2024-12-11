import React from 'react';
import WebSocketService from './services/WebSocketService';
import RoleSelection from './components/RoleSelection';
import CustomerDashboard from './components/CustomerDashboard';
import VendorDashboard from './components/VendorDashboard';
import TicketPoolStatus from './components/TicketPoolStatus';
import LogDisplay from './components/LogDisplay';
import Settings from './components/Settings';
import './App.css';

class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            userRole: null,
            systemStatus: {
                ticketsAvailable: 0,
                maxCapacity: 100,
                logs: []
            },
            isConnected: false,
            webSocketService: new WebSocketService(),
            isSettingsOpen: false,
            systemConfig: {
                maxTicketCapacity: 100,
                ticketsPerRelease: 1,
                releaseInterval: 2,
                purchaseInterval: 2
            }
        };

        // Bind methods
        this.addLog = this.addLog.bind(this);
        this.handleCustomerSubmit = this.handleCustomerSubmit.bind(this);
        this.handleVendorSubmit = this.handleVendorSubmit.bind(this);
        this.handleSettingsSave = this.handleSettingsSave.bind(this);
        this.setupWebSocket = this.setupWebSocket.bind(this);
    }

    componentDidMount() {
        this.setupWebSocket();
    }

    componentWillUnmount() {
        this.state.webSocketService.disconnect();
    }

    setupWebSocket() {
        this.state.webSocketService.connect(
            () => {
                this.setState({ isConnected: true });
                this.addLog('WebSocket connected successfully');
                
                // Load saved settings when connected
                this.state.webSocketService.loadSettings();

                this.state.webSocketService.setTicketUpdateHandler((data) => {
                    console.log('Ticket update received:', data);
                    if (data) {
                        this.setState(prevState => ({
                            systemStatus: {
                                ...prevState.systemStatus,
                                ticketsAvailable: data.availableTickets,
                                maxCapacity: data.maxTicketCapacity || prevState.systemStatus.maxCapacity,
                                logs: [...prevState.systemStatus.logs, `Ticket pool updated: ${data.availableTickets} tickets available`]
                            }
                        }));
                    }
                });

                this.state.webSocketService.setSystemStatusHandler((data) => {
                    console.log('System status received:', data);
                    if (data) {
                        this.setState(prevState => ({
                            systemStatus: {
                                ...prevState.systemStatus,
                                ticketsAvailable: data.availableTickets,
                                maxCapacity: data.maxTicketCapacity || prevState.systemStatus.maxCapacity
                            }
                        }));
                    }
                });

                this.state.webSocketService.setSettingsUpdateHandler((data) => {
                    console.log('Settings update received:', data);
                    if (data) {
                        this.setState(prevState => ({
                            systemConfig: {
                                ...prevState.systemConfig,
                                maxTicketCapacity: data.maxTicketCapacity || prevState.systemConfig.maxTicketCapacity,
                                ticketsPerRelease: data.ticketsPerRelease || prevState.systemConfig.ticketsPerRelease,
                                releaseInterval: data.releaseInterval || prevState.systemConfig.releaseInterval,
                                purchaseInterval: data.purchaseInterval || prevState.systemConfig.purchaseInterval
                            },
                            systemStatus: {
                                ...prevState.systemStatus,
                                maxCapacity: data.maxTicketCapacity || prevState.systemStatus.maxCapacity
                            }
                        }));
                        this.addLog('Settings loaded from server');
                    }
                });
            },
            (error) => {
                console.error('WebSocket connection error:', error);
                this.setState({ isConnected: false });
                this.addLog(`Connection error: ${error.message || 'Unknown error'}`);
            }
        );
    }

    addLog(message) {
        this.setState(prevState => ({
            systemStatus: {
                ...prevState.systemStatus,
                logs: [...prevState.systemStatus.logs, message]
            }
        }));
    }

    handleCustomerSubmit(data) {
        if (this.state.isConnected) {
            this.state.webSocketService.startCustomer({
                totalTickets: parseInt(data.totalTickets),
                ticketsPerRelease: 1,
                releaseInterval: this.state.systemConfig.releaseInterval,
                purchaseInterval: this.state.systemConfig.purchaseInterval,
                maxTicketCapacity: this.state.systemStatus.maxCapacity,
                numCustomers: 1,
                numVendors: 0
            });
            this.addLog(`Customer process started - Requesting ${data.totalTickets} tickets at ${this.state.systemConfig.purchaseInterval} second intervals`);
        }
    }

    handleVendorSubmit(data) {
        if (this.state.isConnected) {
            this.state.webSocketService.startVendor({
                totalTickets: parseInt(data.totalTickets),
                ticketsPerRelease: 1,
                releaseInterval: this.state.systemConfig.releaseInterval,
                maxTicketCapacity: this.state.systemStatus.maxCapacity,
                numVendors: 1,
                numCustomers: 0
            });
            this.addLog(`Vendor process started - Releasing ${data.totalTickets} tickets at ${this.state.systemConfig.releaseInterval} second intervals`);
        }
    }

    handleSettingsSave(newConfig) {
        this.state.webSocketService.saveSettings(newConfig);
        this.setState(prevState => ({
            systemConfig: newConfig,
            systemStatus: {
                ...prevState.systemStatus,
                maxCapacity: newConfig.maxTicketCapacity
            },
            isSettingsOpen: false
        }));
        this.addLog(`System configuration updated - Max Capacity: ${newConfig.maxTicketCapacity}, Release Interval: ${newConfig.releaseInterval}s, Purchase Interval: ${newConfig.purchaseInterval}s`);
    }

    render() {
        if (!this.state.isConnected) {
            return (
                <div className="App">
                    <div className="connection-status">
                        Connecting to system... Please wait.
                    </div>
                </div>
            );
        }

        return (
            <div className="App">
                <h1>Real-Time Ticket System</h1>
                {!this.state.userRole ? (
                    <>
                        <RoleSelection onRoleSelect={(role) => this.setState({ userRole: role })} />
                        <div 
                            className="settings-icon" 
                            onClick={() => this.setState({ isSettingsOpen: true })}
                            style={{
                                position: 'fixed',
                                bottom: '20px',
                                right: '20px',
                                cursor: 'pointer',
                                background: '#007bff',
                                color: 'white',
                                width: '40px',
                                height: '40px',
                                borderRadius: '50%',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '24px',
                                boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
                            }}
                        >
                            ⚙️
                        </div>
                    </>
                ) : (
                    <div className="system-container">
                        <button 
                            onClick={() => this.setState({ userRole: null })} 
                            style={{
                                position: 'absolute',
                                top: '10px',
                                left: '10px',
                                padding: '8px 16px',
                                cursor: 'pointer',
                                backgroundColor: '#f8f9fa',
                                border: '1px solid #ddd',
                                borderRadius: '4px',
                                color: 'black'
                            }}
                        >
                            Back
                        </button>
                        
                        <TicketPoolStatus
                            ticketsAvailable={this.state.systemStatus.ticketsAvailable}
                            maxCapacity={this.state.systemStatus.maxCapacity}
                        />
                        
                        {this.state.userRole === 'customer' ? (
                            <CustomerDashboard
                                webSocketService={this.state.webSocketService}
                                onSubmit={this.handleCustomerSubmit}
                                ticketsAvailable={this.state.systemStatus.ticketsAvailable}
                                isWebSocketConnected={this.state.isConnected}
                            />
                        ) : (
                            <VendorDashboard
                                webSocketService={this.state.webSocketService}
                                onSubmit={this.handleVendorSubmit}
                                ticketsAvailable={this.state.systemStatus.ticketsAvailable}
                                maxCapacity={this.state.systemStatus.maxCapacity}
                                isWebSocketConnected={this.state.isConnected}
                            />
                        )}

                        <LogDisplay logs={this.state.systemStatus.logs} />
                    </div>
                )}
                
                <Settings 
                    isOpen={this.state.isSettingsOpen}
                    onClose={() => this.setState({ isSettingsOpen: false })}
                    onSave={this.handleSettingsSave}
                    currentConfig={this.state.systemConfig}
                />
            </div>
        );
    }
}

export default App;