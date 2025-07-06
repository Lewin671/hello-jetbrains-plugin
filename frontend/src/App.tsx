import React, { useState, useRef, useEffect } from 'react';
import './App.css';

// 声明全局的 sendMessage 函数类型
declare global {
  interface Window {
    sendMessage?: (
      message: string,
      onSuccess: (response: string) => void,
      onFailure: (error: string) => void
    ) => void;
  }
}

interface Message {
  id: string;
  content: string;
  sender: 'user' | 'assistant';
}

function App() {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 'welcome',
      content: '你好！我是你的AI助手，有什么可以帮助你的吗？',
      sender: 'assistant'
    }
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [isDisabled, setIsDisabled] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isTyping]);

  const handleKeyPress = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter' && !isDisabled) {
      send();
    }
  };

  const addMessage = (content: string, sender: 'user' | 'assistant') => {
    const newMessage: Message = {
      id: Date.now().toString(),
      content,
      sender
    };
    setMessages(prev => [...prev, newMessage]);
  };

  const showTypingIndicator = () => {
    setIsTyping(true);
  };

  const hideTypingIndicator = () => {
    setIsTyping(false);
  };

  const setInteractionDisabled = (disabled: boolean) => {
    setIsDisabled(disabled);
    if (!disabled) {
      inputRef.current?.focus();
    }
  };

  const send = () => {
    const message = inputValue.trim();
    if (message === '') return;

    addMessage(message, 'user');
    setInputValue('');
    
    // 禁用输入和按钮，防止重复发送
    setInteractionDisabled(true);
    showTypingIndicator();

    // window.sendMessage 是由 Kotlin 代码注入的桥接函数
    if (window.sendMessage) {
      window.sendMessage(
        message,
        function(response: string) { // onSuccess 回调
          hideTypingIndicator();
          addMessage(response, 'assistant');
          setInteractionDisabled(false);
        },
        function(error: string) { // onFailure 回调
          hideTypingIndicator();
          addMessage(`【错误】${error}`, 'assistant');
          setInteractionDisabled(false);
        }
      );
    } else {
      // 如果不在 JCEF 环境中，或者桥接未初始化，执行此处的备用逻辑
      console.warn("Bridge function 'window.sendMessage' not found. Running in fallback mode.");
      setTimeout(() => {
        hideTypingIndicator();
        addMessage('抱歉，与后端的连接似乎已断开。', 'assistant');
        setInteractionDisabled(false);
      }, 1000);
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-header">🤖 AI 助手</div>
      
      <div className="chat-messages">
        {messages.map((message) => (
          <div key={message.id} className={`message ${message.sender}`}>
            <div className="message-avatar">
              {message.sender === 'user' ? '你' : 'AI'}
            </div>
            <div className="message-content">
              {message.content}
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
      
      {isTyping && (
        <div className="typing-indicator">
          AI 正在思考...
        </div>
      )}
      
      <div className="chat-input">
        <div className="input-container">
          <input
            type="text"
            className="message-input"
            placeholder="输入你的消息..."
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={isDisabled}
            ref={inputRef}
          />
          <button
            className="send-button"
            onClick={send}
            disabled={isDisabled}
          >
            发送
          </button>
        </div>
      </div>
    </div>
  );
}

export default App;
