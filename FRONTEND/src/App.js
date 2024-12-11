import React, { useState, useEffect } from 'react';
import WebSocketService from './services/WebSocketService';
import RoleSelection from './components/RoleSelection';
import CustomerDashboard from './components/CustomerDashboard';
import VendorDashboard from './components/VendorDashboard';
import TicketPoolStatus from './components/TicketPoolStatus';
import LogDisplay from './components/LogDisplay';
import Settings from './components/Settings';
import './App.css';

function App() {
  const [userRole, setUserRole] = useState(null);
  const [systemStatus, setSystemStatus] = useState({
    ticketsAvailable: 0,
    maxCapacity: 100,
    logs: []
  });
  const [isConnected, setIsConnected] = useState(false);
  const [webSocketService] = useState(() => new WebSocketService());
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [systemConfig, setSystemConfig] = useState({
    maxTicketCapacity: 100,
    ticketsPerRelease: 1,
    releaseInterval: 2,
    purchaseInterval: 2
  });

  useEffect(() => {
    const setupWebSocket = () => {
      webSocketService.connect(
        () => {
          setIsConnected(true);
          addLog('WebSocket connected successfully');
          
          // Load saved settings when connected
          webSocketService.loadSettings();

          webSocketService.setTicketUpdateHandler((data) => {
            console.log('Ticket update received:', data);
            if (data) {
              setSystemStatus(prev => {
                const newStatus = {
                  ...prev,
                  ticketsAvailable: data.availableTickets,
                  maxCapacity: data.maxTicketCapacity || prev.maxCapacity,
                  logs: [...prev.logs, `Ticket pool updated: ${data.availableTickets} tickets available`]
                };
                console.log('Setting new system status:', newStatus);
                return newStatus;
              });
            }
          });
          
          webSocketService.setSystemStatusHandler((data) => {
            console.log('System status received:', data);
            if (data) {
              setSystemStatus(prev => {
                const newStatus = {
                  ...prev,
                  ticketsAvailable: data.availableTickets,
                  maxCapacity: data.maxTicketCapacity || prev.maxCapacity
                };
                console.log('Setting new system status:', newStatus);
                return newStatus;
              });
            }
          });

          webSocketService.setSettingsUpdateHandler((data) => {
            console.log('Settings update received:', data);
            if (data) {
              setSystemConfig(prevConfig => ({
                ...prevConfig,
                maxTicketCapacity: data.maxTicketCapacity || prevConfig.maxTicketCapacity,
                ticketsPerRelease: data.ticketsPerRelease || prevConfig.ticketsPerRelease,
                releaseInterval: data.releaseInterval || prevConfig.releaseInterval,
                purchaseInterval: data.purchaseInterval || prevConfig.purchaseInterval
              }));
              setSystemStatus(prevStatus => ({
                ...prevStatus,
                maxCapacity: data.maxTicketCapacity || prevStatus.maxCapacity
              }));
              addLog('Settings loaded from server');
            }
          });
        },
        (error) => {
          console.error('WebSocket connection error:', error);
          setIsConnected(false);
          addLog(`Connection error: ${error.message || 'Unknown error'}`);
        }
      );
    };

    setupWebSocket();

    return () => {
      webSocketService.disconnect();
    };
  }, [webSocketService]);

  const addLog = (message) => {
    setSystemStatus(prevStatus => ({
      ...prevStatus,
      logs: [...prevStatus.logs, message]
    }));
  };

  const handleCustomerSubmit = (data) => {
    if (isConnected) {
      webSocketService.startCustomer({
        totalTickets: parseInt(data.totalTickets),
        ticketsPerRelease: 1,
        releaseInterval: systemConfig.releaseInterval,
        purchaseInterval: systemConfig.purchaseInterval,
        maxTicketCapacity: systemStatus.maxCapacity,
        numCustomers: 1,
        numVendors: 0
      });
      addLog(`Customer process started - Requesting ${data.totalTickets} tickets at ${systemConfig.purchaseInterval} second intervals`);
    }
  };

  const handleVendorSubmit = (data) => {
    if (isConnected) {
      webSocketService.startVendor({
        totalTickets: parseInt(data.totalTickets),
        ticketsPerRelease: 1,
        releaseInterval: systemConfig.releaseInterval,
        maxTicketCapacity: systemStatus.maxCapacity,
        numVendors: 1,
        numCustomers: 0
      });
      addLog(`Vendor process started - Releasing ${data.totalTickets} tickets at ${systemConfig.releaseInterval} second intervals`);
    }
  };

  const handleSettingsSave = (newConfig) => {
    webSocketService.saveSettings(newConfig);
    setSystemConfig(newConfig);
    setSystemStatus(prevStatus => ({
      ...prevStatus,
      maxCapacity: newConfig.maxTicketCapacity
    }));
    addLog(`System configuration updated - Max Capacity: ${newConfig.maxTicketCapacity}, Release Interval: ${newConfig.releaseInterval}s, Purchase Interval: ${newConfig.purchaseInterval}s`);
    setIsSettingsOpen(false);
  };

  if (!isConnected) {
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
      {!userRole ? (
        <>
          <RoleSelection onRoleSelect={setUserRole} />
          <div 
            className="settings-icon" 
            onClick={() => setIsSettingsOpen(true)}
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
            onClick={() => setUserRole(null)} 
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
            ticketsAvailable={systemStatus.ticketsAvailable}
            maxCapacity={systemStatus.maxCapacity}
          />
          
          {userRole === 'customer' ? (
            <CustomerDashboard
              webSocketService={webSocketService}
              onSubmit={handleCustomerSubmit}
              ticketsAvailable={systemStatus.ticketsAvailable}
              isWebSocketConnected={isConnected}
            />
          ) : (
            <VendorDashboard
              webSocketService={webSocketService}
              onSubmit={handleVendorSubmit}
              ticketsAvailable={systemStatus.ticketsAvailable}
              maxCapacity={systemStatus.maxCapacity}
              isWebSocketConnected={isConnected}
            />
          )}

          <LogDisplay logs={systemStatus.logs} />
        </div>
      )}
      
      <Settings 
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        onSave={handleSettingsSave}
        currentConfig={systemConfig}
      />
    </div>
  );
}

export default App;