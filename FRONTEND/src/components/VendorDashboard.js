import React, { useState } from 'react';

function VendorDashboard({ onSubmit, ticketsAvailable, maxCapacity, isWebSocketConnected }) {
  const [totalTickets, setTotalTickets] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (!totalTickets) {
      alert('Please enter the number of tickets to release');
      return;
    }

    if (!isWebSocketConnected) {
      alert('WebSocket is not connected. Please try again.');
      return;
    }

    // Check if new tickets would exceed maximum capacity
    const newTotal = parseInt(totalTickets) + ticketsAvailable;
    if (newTotal > maxCapacity) {
      setErrorMessage(`Cannot add ${totalTickets} tickets. Would exceed maximum capacity of ${maxCapacity}. Current available: ${ticketsAvailable}`);
      return;
    }

    onSubmit({ totalTickets: parseInt(totalTickets) });
    setTotalTickets('');
  };

  return (
    <div className="vendor-dashboard">
      <h2>Vendor Dashboard</h2>
      <div className="ticket-management">
        <div className="status-info">
          <p>Current Pool: {ticketsAvailable} tickets</p>
          <p>Maximum Capacity: {maxCapacity} tickets</p>
          {errorMessage && (
            <div className="error-message" style={{ color: 'red', margin: '10px 0', fontWeight: 'bold' }}>
              {errorMessage}
            </div>
          )}
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Total Tickets to Release:</label>
            <input
              type="number"
              min="1"
              max={maxCapacity - ticketsAvailable}
              value={totalTickets}
              onChange={(e) => {
                setTotalTickets(e.target.value);
                setErrorMessage('');
              }}
            />
            <small style={{ display: 'block', color: 'gray' }}>
              Maximum allowed: {maxCapacity - ticketsAvailable} tickets
            </small>
          </div>
          <button 
            type="submit"
            disabled={!isWebSocketConnected || ticketsAvailable >= maxCapacity}
          >
            Start Vendor Process
          </button>
          {ticketsAvailable >= maxCapacity && (
            <div style={{ color: 'red', marginTop: '10px' }}>
              Maximum capacity reached. Cannot add more tickets.
            </div>
          )}
        </form>
      </div>
    </div>
  );
}

export default VendorDashboard;