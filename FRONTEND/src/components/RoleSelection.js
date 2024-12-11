import React from 'react';

class RoleSelection extends React.Component {
    constructor(props) {
        super(props);
        this.handleRoleSelect = this.handleRoleSelect.bind(this);
    }

    handleRoleSelect(role) {
        this.props.onRoleSelect(role);
    }

    render() {
        return (
            <div className="role-selection">
                <h1>Welcome to the Ticket System</h1>
                <div className="role-buttons">
                    <button 
                        className="role-button customer"
                        onClick={() => this.handleRoleSelect('customer')}
                    >
                        I'm a Customer
                    </button>
                    <button 
                        className="role-button vendor"
                        onClick={() => this.handleRoleSelect('vendor')}
                    >
                        I'm a Vendor
                    </button>
                </div>
            </div>
        );
    }
}

export default RoleSelection;