import React, { useState } from 'react';

function Settings({ isOpen, onClose, onSave, currentConfig }) {
  const [config, setConfig] = useState({
    maxTicketCapacity: currentConfig?.maxTicketCapacity || 100,
    ticketsPerRelease: currentConfig?.ticketsPerRelease || 1,
    releaseInterval: currentConfig?.releaseInterval || 2,
    purchaseInterval: currentConfig?.purchaseInterval || 2
  });

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(config);
    onClose();
  };

  const modalStyle = {
    position: 'fixed',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000
  };

  const contentStyle = {
    background: 'white',
    padding: '20px',
    borderRadius: '8px',
    width: '90%',
    maxWidth: '500px'
  };

  const formGroupStyle = {
    marginBottom: '15px'
  };

  const labelStyle = {
    display: 'block',
    marginBottom: '5px'
  };

  const inputStyle = {
    width: '100%',
    padding: '8px',
    marginBottom: '10px',
    borderRadius: '4px',
    border: '1px solid #ddd'
  };

  const buttonStyle = {
    padding: '8px 16px',
    margin: '0 10px',
    borderRadius: '4px',
    cursor: 'pointer'
  };

  const saveButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none'
  };

  const cancelButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#f8f9fa',
    border: '1px solid #ddd',
    color: 'black'
  };

  return (
    <div style={modalStyle}>
      <div style={contentStyle}>
        <h2 style={{ marginBottom: '20px' }}>System Settings</h2>
        <form onSubmit={handleSubmit}>
          <div style={formGroupStyle}>
            <label style={labelStyle}>Maximum Ticket Capacity:</label>
            <input
              type="number"
              min="1"
              style={inputStyle}
              value={config.maxTicketCapacity}
              onChange={(e) => setConfig({
                ...config,
                maxTicketCapacity: parseInt(e.target.value)
              })}
            />
          </div>
          <div style={formGroupStyle}>
            <label style={labelStyle}>Tickets Per Release:</label>
            <input
              type="number"
              min="1"
              style={inputStyle}
              value={config.ticketsPerRelease}
              onChange={(e) => setConfig({
                ...config,
                ticketsPerRelease: parseInt(e.target.value)
              })}
            />
          </div>
          <div style={formGroupStyle}>
            <label style={labelStyle}>Vendor Release Interval (seconds):</label>
            <input
              type="number"
              min="1"
              style={inputStyle}
              value={config.releaseInterval}
              onChange={(e) => setConfig({
                ...config,
                releaseInterval: parseInt(e.target.value)
              })}
            />
          </div>
          <div style={formGroupStyle}>
            <label style={labelStyle}>Customer Purchase Interval (seconds):</label>
            <input
              type="number"
              min="1"
              style={inputStyle}
              value={config.purchaseInterval}
              onChange={(e) => setConfig({
                ...config,
                purchaseInterval: parseInt(e.target.value)
              })}
            />
          </div>
          <div style={{ textAlign: 'right', marginTop: '20px' }}>
            <button type="button" onClick={onClose} style={cancelButtonStyle}>
              Cancel
            </button>
            <button type="submit" style={saveButtonStyle}>
              Save Changes
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default Settings;