import React from 'react';

class Settings extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            config: {
                maxTicketCapacity: props.currentConfig?.maxTicketCapacity || 100,
                ticketsPerRelease: props.currentConfig?.ticketsPerRelease || 1,
                releaseInterval: props.currentConfig?.releaseInterval || 2,
                purchaseInterval: props.currentConfig?.purchaseInterval || 2
            }
        };

        // Bind methods
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
    }

    handleSubmit(e) {
        e.preventDefault();
        this.props.onSave(this.state.config);
        this.props.onClose();
    }

    handleInputChange(field, value) {
        this.setState(prevState => ({
            config: {
                ...prevState.config,
                [field]: parseInt(value)
            }
        }));
    }

    // Styles object to keep render method clean
    styles = {
        modal: {
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
        },
        content: {
            background: 'white',
            padding: '20px',
            borderRadius: '8px',
            width: '90%',
            maxWidth: '500px'
        },
        formGroup: {
            marginBottom: '15px'
        },
        label: {
            display: 'block',
            marginBottom: '5px'
        },
        input: {
            width: '100%',
            padding: '8px',
            marginBottom: '10px',
            borderRadius: '4px',
            border: '1px solid #ddd'
        },
        button: {
            padding: '8px 16px',
            margin: '0 10px',
            borderRadius: '4px',
            cursor: 'pointer'
        },
        saveButton: {
            padding: '8px 16px',
            margin: '0 10px',
            borderRadius: '4px',
            cursor: 'pointer',
            backgroundColor: '#007bff',
            color: 'white',
            border: 'none'
        },
        cancelButton: {
            padding: '8px 16px',
            margin: '0 10px',
            borderRadius: '4px',
            cursor: 'pointer',
            backgroundColor: '#f8f9fa',
            border: '1px solid #ddd',
            color: 'black'
        }
    };

    render() {
        const { isOpen, onClose } = this.props;
        const { config } = this.state;

        if (!isOpen) return null;

        return (
            <div style={this.styles.modal}>
                <div style={this.styles.content}>
                    <h2 style={{ marginBottom: '20px' }}>System Settings</h2>
                    <form onSubmit={this.handleSubmit}>
                        <div style={this.styles.formGroup}>
                            <label style={this.styles.label}>Maximum Ticket Capacity:</label>
                            <input
                                type="number"
                                min="1"
                                style={this.styles.input}
                                value={config.maxTicketCapacity}
                                onChange={(e) => this.handleInputChange('maxTicketCapacity', e.target.value)}
                            />
                        </div>
                        <div style={this.styles.formGroup}>
                            <label style={this.styles.label}>Tickets Per Release:</label>
                            <input
                                type="number"
                                min="1"
                                style={this.styles.input}
                                value={config.ticketsPerRelease}
                                onChange={(e) => this.handleInputChange('ticketsPerRelease', e.target.value)}
                            />
                        </div>
                        <div style={this.styles.formGroup}>
                            <label style={this.styles.label}>Vendor Release Interval (seconds):</label>
                            <input
                                type="number"
                                min="1"
                                style={this.styles.input}
                                value={config.releaseInterval}
                                onChange={(e) => this.handleInputChange('releaseInterval', e.target.value)}
                            />
                        </div>
                        <div style={this.styles.formGroup}>
                            <label style={this.styles.label}>Customer Purchase Interval (seconds):</label>
                            <input
                                type="number"
                                min="1"
                                style={this.styles.input}
                                value={config.purchaseInterval}
                                onChange={(e) => this.handleInputChange('purchaseInterval', e.target.value)}
                            />
                        </div>
                        <div style={{ textAlign: 'right', marginTop: '20px' }}>
                            <button type="button" onClick={onClose} style={this.styles.cancelButton}>
                                Cancel
                            </button>
                            <button type="submit" style={this.styles.saveButton}>
                                Save Changes
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        );
    }
}

export default Settings;