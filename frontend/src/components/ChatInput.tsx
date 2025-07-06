import React, { forwardRef } from 'react';
import { CHAT_CONSTANTS } from '../constants/chat';

interface ChatInputProps {
  value: string;
  onChange: (value: string) => void;
  onSend: () => void;
  disabled: boolean;
  placeholder?: string;
}

export const ChatInput = forwardRef<HTMLInputElement, ChatInputProps>(
  ({ value, onChange, onSend, disabled, placeholder = CHAT_CONSTANTS.INPUT_PLACEHOLDER }, ref) => {
    const handleKeyPress = (event: React.KeyboardEvent) => {
      if (event.key === 'Enter' && !disabled) {
        onSend();
      }
    };

    return (
      <div className="chat-input">
        <div className="input-container">
          <input
            type="text"
            className="message-input"
            placeholder={placeholder}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={disabled}
            ref={ref}
          />
          <button
            className="send-button"
            onClick={onSend}
            disabled={disabled}
          >
            {CHAT_CONSTANTS.SEND_BUTTON_TEXT}
          </button>
        </div>
      </div>
    );
  }
); 