import React from 'react';
import { Message } from '../types/chat';

interface MessageItemProps {
  message: Message;
}

export const MessageItem: React.FC<MessageItemProps> = ({ message }) => {
  const isUser = message.sender === 'user';
  
  return (
    <div className={`message ${message.sender}`}>
      <div className="message-avatar">
        {isUser ? '你' : 'AI'}
      </div>
      <div className="message-content">
        {message.content}
      </div>
    </div>
  );
}; 