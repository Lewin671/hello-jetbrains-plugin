import React, { useRef } from 'react';
import './App.css';
import { useChat } from './hooks/useChat';
import { useAutoScroll } from './hooks/useAutoScroll';
import { ChatService } from './services/chatService';
import { MessageItem } from './components/MessageItem';
import { ChatInput } from './components/ChatInput';
import { TypingIndicator } from './components/TypingIndicator';
import { CHAT_CONSTANTS } from './constants/chat';
import { isValidMessage, focusInput } from './utils/chatUtils';

function App() {
  const {
    messages,
    inputValue,
    isTyping,
    isDisabled,
    addMessage,
    setInputValue,
    setIsTyping,
    setIsDisabled,
    clearInput
  } = useChat();

  const inputRef = useRef<HTMLInputElement>(null);
  const scrollRef = useAutoScroll([messages, isTyping]);

  const handleSend = () => {
    if (!isValidMessage(inputValue)) return;

    addMessage(inputValue.trim(), 'user');
    clearInput();
    
    // 禁用输入和按钮，防止重复发送
    setIsDisabled(true);
    setIsTyping(true);

    ChatService.sendMessage(
      inputValue.trim(),
      {
        onSuccess: (response: string) => {
          setIsTyping(false);
          addMessage(response, 'assistant');
          setIsDisabled(false);
          focusInput(inputRef);
        },
        onFailure: (error: string) => {
          setIsTyping(false);
          addMessage(`${CHAT_CONSTANTS.ERROR_PREFIX}${error}`, 'assistant');
          setIsDisabled(false);
          focusInput(inputRef);
        }
      }
    );
  };

  return (
    <div className="chat-container">
      <div className="chat-header">{CHAT_CONSTANTS.HEADER_TEXT}</div>
      
      <div className="chat-messages">
        {messages.map((message) => (
          <MessageItem key={message.id} message={message} />
        ))}
        <div ref={scrollRef} />
      </div>
      
      <TypingIndicator isTyping={isTyping} />
      
      <ChatInput
        value={inputValue}
        onChange={setInputValue}
        onSend={handleSend}
        disabled={isDisabled}
        ref={inputRef}
      />
    </div>
  );
}

export default App;
