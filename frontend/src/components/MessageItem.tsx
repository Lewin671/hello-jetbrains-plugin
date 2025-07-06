import React, { useState } from 'react';
import { Message } from '../types/chat';

interface MessageItemProps {
  message: Message;
}

export const MessageItem: React.FC<MessageItemProps> = ({ message }) => {
  const isUser = message.sender === 'user';
  const hasToolCall = message.toolCall;
  const [expanded, setExpanded] = useState(false);
  
  const toggleExpand = () => setExpanded(prev => !prev);
  
  const renderToolCall = () => {
    if (!hasToolCall) return null;
    
    return (
      <div className={`tool-call-info ${expanded ? 'expanded' : 'collapsed'}`}>        
        <div className="tool-header" onClick={toggleExpand} style={{cursor: 'pointer'}}>
          <span className="tool-arrow">{expanded ? '▼' : '▶'}</span>
          <span className="tool-icon">🔧</span>
          <span className="tool-name">{hasToolCall.toolName}</span>
        </div>
        {expanded && (
          <div className="tool-details">
            <div className="tool-input">
              <strong>输入:</strong> {JSON.stringify(hasToolCall.toolInput, null, 2)}
            </div>
            <div className="tool-output">
              <strong>输出:</strong> {hasToolCall.toolOutput}
            </div>
          </div>
        )}
      </div>
    );
  };
  
  return (
    <div className={`message ${message.sender} ${hasToolCall ? 'with-tool-call' : ''}`}>
      <div className="message-avatar">
        {isUser ? '你' : 'AI'}
      </div>
      <div className="message-content">
        {message.content}
        {renderToolCall()}
      </div>
    </div>
  );
}; 