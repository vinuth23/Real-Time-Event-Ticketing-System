import React from 'react';

class LogDisplay extends React.Component {
    render() {
        const { logs } = this.props;
        
        return (
            <div className="log-display">
                <h2>System Logs</h2>
                <div className="log-container">
                    {logs.length === 0 ? (
                        <p>No logs available</p>
                    ) : (
                        <ul>
                            {logs.slice(-10).map((log, index) => (
                                <li 
                                    key={index} 
                                    className={`log-entry ${
                                        log.includes('error') ? 'error' : 
                                        log.includes('started') ? 'success' : ''
                                    }`}
                                >
                                    {log}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        );
    }
}

export default LogDisplay;