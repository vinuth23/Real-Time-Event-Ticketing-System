import React, { useState } from 'react';

function CustomerDashboard({ onSubmit, ticketsAvailable, isWebSocketConnected }) {
  const [totalTickets, setTotalTickets] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (!totalTickets) {
      alert('Please enter the number of tickets to purchase');
      return;
    }

    if (!isWebSocketConnected) {
      alert('WebSocket is not connected. Please try again.');
      return;
    }

    if (parseInt(totalTickets) > ticketsAvailable) {
      setErrorMessage(`Cannot purchase ${totalTickets} tickets. Only ${ticketsAvailable} tickets available.`);
      return;
    }

    onSubmit({ totalTickets: parseInt(totalTickets) });
    setTotalTickets(''); // Clear input after submission
  };

  return (
    <div className="customer-dashboard">
      <h2>Customer Dashboard</h2>
      <div className="ticket-info">
        <h3>Available Tickets: {ticketsAvailable}</h3>
        {errorMessage && (
          <div className="error-message" style={{ color: 'red', margin: '10px 0', fontWeight: 'bold' }}>
            {errorMessage}
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Tickets to Purchase:</label>
            <input
              type="number"
              min="1"
              max={ticketsAvailable}
              value={totalTickets}
              onChange={(e) => {
                setTotalTickets(e.target.value);
                setErrorMessage('');
              }}
              required
            />
            <small style={{ display: 'block', color: 'gray' }}>
              Maximum allowed: {ticketsAvailable} tickets
            </small>
          </div>
          <button 
            type="submit"
            disabled={!isWebSocketConnected || ticketsAvailable === 0}
          >
            Start Customer Process
          </button>
          {ticketsAvailable === 0 && (
            <div style={{ color: 'red', marginTop: '10px' }}>
              No tickets available for purchase
            </div>
          )}
        </form>
      </div>
    </div>
  );
}

export default CustomerDashboard;