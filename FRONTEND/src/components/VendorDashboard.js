import React from 'react';

class VendorDashboard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            totalTickets: '',
            errorMessage: ''
        };
        // Binding methods
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
    }

    handleSubmit(e) {
        e.preventDefault();
        this.setState({ errorMessage: '' });

        if (!this.state.totalTickets) {
            alert('Please enter the number of tickets to release');
            return;
        }

        if (!this.props.isWebSocketConnected) {
            alert('WebSocket is not connected. Please try again.');
            return;
        }

        // Check if new tickets would exceed maximum capacity
        const newTotal = parseInt(this.state.totalTickets) + this.props.ticketsAvailable;
        if (newTotal > this.props.maxCapacity) {
            this.setState({
                errorMessage: `Cannot add ${this.state.totalTickets} tickets. Would exceed maximum capacity of ${this.props.maxCapacity}. Current available: ${this.props.ticketsAvailable}`
            });
            return;
        }

        this.props.onSubmit({ totalTickets: parseInt(this.state.totalTickets) });
        this.setState({ totalTickets: '' });
    }

    handleInputChange(e) {
        this.setState({
            totalTickets: e.target.value,
            errorMessage: ''
        });
    }

    render() {
        const { ticketsAvailable, maxCapacity, isWebSocketConnected } = this.props;
        const { totalTickets, errorMessage } = this.state;

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
                    <form onSubmit={this.handleSubmit}>
                        <div className="form-group">
                            <label>Total Tickets to Release:</label>
                            <input
                                type="number"
                                min="1"
                                max={maxCapacity - ticketsAvailable}
                                value={totalTickets}
                                onChange={this.handleInputChange}
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
}

export default VendorDashboard;