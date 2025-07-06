import React, { useState } from 'react';
import { Message } from '../types/chat';

interface MessageItemProps {
  message: Message;
}

export const MessageItem: React.FC<MessageItemProps> = ({ message }) => {
  const isUser = message.sender === 'user';
  const hasToolCall = message.toolCall;
  const hasToolCalls = message.toolCalls && message.toolCalls.length > 0;
  const [expanded, setExpanded] = useState(false);
  
  // 添加调试日志
  console.log('🔍 MessageItem rendering:', {
    messageId: message.id,
    content: message.content,
    hasToolCall,
    hasToolCalls,
    toolCalls: message.toolCalls,
    toolCallsLength: message.toolCalls?.length
  });
  
  const toggleExpand = () => setExpanded(prev => !prev);
  
  const renderToolCall = () => {
    if (!hasToolCall && !hasToolCalls) return null;
    
    // 支持多个工具调用
    const toolCalls = message.toolCalls || (hasToolCall ? [hasToolCall] : []);
    
    return (
      <div className="tool-calls-container">
        {toolCalls.map((toolCall, index) => (
          <div key={index} className={`tool-call-info ${expanded ? 'expanded' : 'collapsed'}`}>        
            <div className="tool-header" onClick={toggleExpand} style={{cursor: 'pointer'}}>
              <span className="tool-arrow">{expanded ? '▼' : '▶'}</span>
              <span className="tool-icon">🔧</span>
              <span className="tool-name">{toolCall.toolName}</span>
            </div>
            {expanded && (
              <div className="tool-details">
                <div className="tool-input">
                  <strong>输入:</strong> {JSON.stringify(toolCall.toolInput, null, 2)}
                </div>
                <div className="tool-output">
                  <strong>输出:</strong> {toolCall.toolOutput}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    );
  };
  
  return (
    <div className={`message ${message.sender} ${hasToolCall || hasToolCalls ? 'with-tool-call' : ''}`}>
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