import React from 'react';

class CustomerDashboard extends React.Component {
   constructor(props) {
       super(props);
       this.state = {
           totalTickets: '',
           errorMessage: ''
       };
       // Bind methods
       this.handleSubmit = this.handleSubmit.bind(this);
       this.handleInputChange = this.handleInputChange.bind(this);
   }

   handleSubmit(e) {
       e.preventDefault();
       this.setState({ errorMessage: '' });

       if (!this.state.totalTickets) {
           alert('Please enter the number of tickets to purchase');
           return;
       }

       if (!this.props.isWebSocketConnected) {
           alert('WebSocket is not connected. Please try again.');
           return;
       }

       if (parseInt(this.state.totalTickets) > this.props.ticketsAvailable) {
           this.setState({
               errorMessage: `Cannot purchase ${this.state.totalTickets} tickets. Only ${this.props.ticketsAvailable} tickets available.`
           });
           return;
       }

       this.props.onSubmit({ totalTickets: parseInt(this.state.totalTickets) });
       this.setState({ totalTickets: '' }); // Clear input after submission
   }

   handleInputChange(e) {
       this.setState({
           totalTickets: e.target.value,
           errorMessage: ''
       });
   }

   render() {
       const { ticketsAvailable, isWebSocketConnected } = this.props;
       const { totalTickets, errorMessage } = this.state;

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
                   <form onSubmit={this.handleSubmit}>
                       <div className="form-group">
                           <label>Tickets to Purchase:</label>
                           <input
                               type="number"
                               min="1"
                               max={ticketsAvailable}
                               value={totalTickets}
                               onChange={this.handleInputChange}
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
}

export default CustomerDashboard;