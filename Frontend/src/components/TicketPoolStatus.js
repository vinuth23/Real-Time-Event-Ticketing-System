import React from 'react';

const TicketPoolStatus = ({ ticketsAvailable, maxCapacity }) => {
  console.log('TicketPoolStatus received props:', { ticketsAvailable, maxCapacity });

  const calculatePercentage = () => {
    if (maxCapacity === 0) return 0;
    return Math.round((ticketsAvailable / maxCapacity) * 100);
  };

  const percentage = calculatePercentage();
  const progressBarColor = 
    percentage > 70 ? 'green' : percentage > 30 ? 'orange' : 'red';

  return (
    <div className="ticket-pool-status">
      <h2>Ticket Pool Status</h2>
      <div className="status-details">
        <div className="status-item">
          <span>Tickets Available:</span>
          <strong>{ticketsAvailable}</strong>
        </div>
        <div className="status-item">
          <span>Maximum Capacity:</span>
          <strong>{maxCapacity || 'Not Set'}</strong>
        </div>
        <div className="status-progress">
          <div 
            className="progress-bar"
            style={{
              width: `${percentage}%`,
              backgroundColor: progressBarColor
            }}
          >
            {percentage}%
          </div>
        </div>
      </div>
    </div>
  );
};

export default TicketPoolStatus;