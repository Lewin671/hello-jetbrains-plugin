import React from 'react';
import { CHAT_CONSTANTS } from '../constants/chat';

interface TypingIndicatorProps {
  isTyping: boolean;
  text?: string;
}

export const TypingIndicator: React.FC<TypingIndicatorProps> = ({ 
  isTyping, 
  text = CHAT_CONSTANTS.TYPING_TEXT 
}) => {
  if (!isTyping) return null;

  return (
    <div className="typing-indicator">
      {text}
    </div>
  );
}; 