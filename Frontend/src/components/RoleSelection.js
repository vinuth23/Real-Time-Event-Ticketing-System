import React from 'react';

const RoleSelection = ({ onRoleSelect }) => {
  return (
    <div className="role-selection">
      <h1>Welcome to the Ticket System</h1>
      <div className="role-buttons">
        <button 
          className="role-button customer"
          onClick={() => onRoleSelect('customer')}
        >
          I'm a Customer
        </button>
        <button 
          className="role-button vendor"
          onClick={() => onRoleSelect('vendor')}
        >
          I'm a Vendor
        </button>
      </div>
    </div>
  );
};

export default RoleSelection;