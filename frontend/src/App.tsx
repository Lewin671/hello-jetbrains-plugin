import React, { useRef } from 'react';
import './App.css';
import { useChat } from './hooks/useChat';
import { useAutoScroll } from './hooks/useAutoScroll';
import { MessageItem } from './components/MessageItem';
import { ChatInput } from './components/ChatInput';
import { TypingIndicator } from './components/TypingIndicator';
import { CHAT_CONSTANTS } from './constants/chat';
import { isValidMessage, focusInput } from './utils/chatUtils';
import { testOllamaAndAgent, testStreamingAgent, testOllamaStreaming } from './services/ollamaTest';
import { LangGraphAgentService } from './services/langGraphAgentService';
import './testTools'; // 导入测试脚本

function App() {
  const {
    messages,
    inputValue,
    isTyping,
    isDisabled,
    setInputValue,
    sendMessage
  } = useChat();

  const inputRef = useRef<HTMLInputElement>(null);
  const scrollRef = useAutoScroll([messages, isTyping]);

  const handleSend = () => {
    if (!isValidMessage(inputValue)) return;
    
    sendMessage(inputValue.trim());
    focusInput(inputRef);
  };

  const handleTest = async () => {
    console.log('开始测试...');
    const result = await testOllamaAndAgent();
    console.log('测试结果:', result);
  };

  const handleStreamingTest = async () => {
    console.log('开始流式测试...');
    const result = await testStreamingAgent();
    console.log('流式测试结果:', result);
  };

  const handleOllamaStreamingTest = async () => {
    console.log('开始 Ollama 流式测试...');
    const result = await testOllamaStreaming();
    console.log('Ollama 流式测试结果:', result);
  };

  const handleTestTools = async () => {
    console.log('开始测试 Tools...');
    await LangGraphAgentService.testTools();
  };

  return (
    <div className="chat-container">
      <div className="chat-header">
        {CHAT_CONSTANTS.HEADER_TEXT}
        <button 
          onClick={handleTest}
          style={{
            marginLeft: '10px',
            padding: '5px 10px',
            fontSize: '12px',
            backgroundColor: '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          测试Agent
        </button>
        <button 
          onClick={handleStreamingTest}
          style={{
            marginLeft: '5px',
            padding: '5px 10px',
            fontSize: '12px',
            backgroundColor: '#28a745',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          测试流式
        </button>
        <button 
          onClick={handleOllamaStreamingTest}
          style={{
            marginLeft: '5px',
            padding: '5px 10px',
            fontSize: '12px',
            backgroundColor: '#ffc107',
            color: 'black',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          测试Ollama流式
        </button>
        <button 
          onClick={handleTestTools}
          style={{
            marginLeft: '5px',
            padding: '5px 10px',
            fontSize: '12px',
            backgroundColor: '#dc3545',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          测试Tools
        </button>
      </div>
      
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
