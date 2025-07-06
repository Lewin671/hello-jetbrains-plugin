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
    sendMessage,
    addMessage,
    addToolCallMessage
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

  const handleDemoToolCall = () => {
    console.log('演示工具调用...');
    sendMessage('今天天气怎么样？请帮我搜索一下。');
  };

  const handleSimpleToolTest = () => {
    console.log('简单工具调用测试...');
    // 直接创建一个工具调用消息来测试显示
    const testToolCall = {
      toolName: 'search',
      toolInput: { query: '今天天气怎么样？' },
      toolOutput: '今天天气晴朗，温度25度，适合外出活动。'
    };
    
    // 使用addToolCallMessage函数来测试工具调用显示
    addToolCallMessage(testToolCall);
    console.log('🔧 Tool call message added:', testToolCall);
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
        <button 
          onClick={handleDemoToolCall}
          style={{
            marginLeft: '5px',
            padding: '5px 10px',
            fontSize: '12px',
            backgroundColor: '#6f42c1',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          演示工具调用
        </button>
        <button 
          onClick={handleSimpleToolTest}
          style={{
            marginLeft: '5px',
            padding: '5px 10px',
            fontSize: '12px',
            backgroundColor: '#fd7e14',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          简单工具测试
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
