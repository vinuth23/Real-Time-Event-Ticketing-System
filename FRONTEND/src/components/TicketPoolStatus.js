import React from 'react';

class TicketPoolStatus extends React.Component {
    constructor(props) {
        super(props);
        this.calculatePercentage = this.calculatePercentage.bind(this);
    }

    calculatePercentage() {
        const { ticketsAvailable, maxCapacity } = this.props;
        if (maxCapacity === 0) return 0;
        return Math.round((ticketsAvailable / maxCapacity) * 100);
    }

    render() {
        console.log('TicketPoolStatus received props:', { 
            ticketsAvailable: this.props.ticketsAvailable, 
            maxCapacity: this.props.maxCapacity 
        });

        const { ticketsAvailable, maxCapacity } = this.props;
        const percentage = this.calculatePercentage();
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
    }
}

export default TicketPoolStatus;