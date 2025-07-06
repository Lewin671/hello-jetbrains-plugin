import { useState, useCallback, useEffect } from 'react';
import { ChatState } from '../types/chat';
import { CHAT_CONSTANTS } from '../constants/chat';
import { createMessage } from '../utils/chatUtils';
import { ChatService } from '../services/chatService';

export const useChat = () => {
  const [state, setState] = useState<ChatState>({
    messages: [CHAT_CONSTANTS.INITIAL_MESSAGE],
    inputValue: '',
    isTyping: false,
    isDisabled: false
  });

  // 初始化LangGraph Agent
  useEffect(() => {
    ChatService.initializeAgent();
  }, []);

  const addMessage = useCallback((content: string, sender: 'user' | 'assistant') => {
    const newMessage = createMessage(content, sender);
    setState(prev => ({
      ...prev,
      messages: [...prev.messages, newMessage]
    }));
  }, []);

  const addToolCallMessage = useCallback((toolCall: { toolName: string; toolInput: any; toolOutput: string }) => {
    const toolMessage = createMessage(
      `🔧 使用了 ${toolCall.toolName} 工具\n输入: ${JSON.stringify(toolCall.toolInput)}\n输出: ${toolCall.toolOutput}`,
      'assistant'
    );
    toolMessage.toolCall = {
      toolName: toolCall.toolName,
      toolInput: toolCall.toolInput,
      toolOutput: toolCall.toolOutput,
      timestamp: new Date().toISOString()
    };
    setState(prev => ({
      ...prev,
      messages: [...prev.messages, toolMessage]
    }));
  }, []);

  const setInputValue = useCallback((value: string) => {
    setState(prev => ({ ...prev, inputValue: value }));
  }, []);

  const setIsTyping = useCallback((typing: boolean) => {
    setState(prev => ({ ...prev, isTyping: typing }));
  }, []);

  const setIsDisabled = useCallback((disabled: boolean) => {
    setState(prev => ({ ...prev, isDisabled: disabled }));
  }, []);

  const clearInput = useCallback(() => {
    setState(prev => ({ ...prev, inputValue: '' }));
  }, []);

  const sendMessage = useCallback(async (message: string) => {
    if (!message.trim()) return;

    console.log('useChat.sendMessage called with:', message);

    // 添加用户消息
    addMessage(message, 'user');
    clearInput();
    setIsTyping(true);
    setIsDisabled(true);

    let assistantMessage = '';
    let isFirstChunk = true;

    try {
      console.log('Calling ChatService.sendMessageWithStreaming...');
      await ChatService.sendMessageWithStreaming(
        message,
        {
          onChunk: (chunk: string) => {
            console.log('onChunk called with:', chunk, 'isFirstChunk:', isFirstChunk);

            // 检测是否为工具调用消息（但不在这里创建工具调用消息，由onToolCall回调处理）
            const TOOL_PREFIX = '🔧 使用了 ';
            if (chunk.startsWith(TOOL_PREFIX)) {
              // 工具调用消息不作为普通文本消息处理，直接返回
              console.log('🔧 Tool call chunk detected, will be handled by onToolCall callback');
              return;
            }

            if (isFirstChunk) {
              // 第一个chunk时创建助手消息
              assistantMessage = chunk;
              console.log('Creating first assistant message:', assistantMessage);

              const newMsg = createMessage(assistantMessage, 'assistant');
              setState(prev => ({ ...prev, messages: [...prev.messages, newMsg] }));
              isFirstChunk = false;
            } else {
              // 后续chunk时更新最后一条消息
              assistantMessage += chunk;
              console.log('Updating assistant message:', assistantMessage);
              setState(prev => ({
                ...prev,
                messages: prev.messages.map((msg, index) => 
                  index === prev.messages.length - 1 
                    ? { ...msg, content: assistantMessage }
                    : msg
                )
              }));
            }
          },
          onToolCall: (toolCall) => {
            console.log('🔧 Tool call detected:', toolCall);
            // 创建工具调用消息
            addToolCallMessage(toolCall);
          },
          onSuccess: (finalMessage: string) => {
            // 流式响应完成
            console.log('onSuccess called with:', finalMessage);
            setIsTyping(false);
            setIsDisabled(false);
            console.log('Message sent successfully:', finalMessage);
          },
          onFailure: (error: string) => {
            console.error('onFailure called with:', error);
            setIsTyping(false);
            setIsDisabled(false);
            addMessage(`错误: ${error}`, 'assistant');
          }
        }
      );
    } catch (error) {
      console.error('Unexpected error in sendMessage:', error);
      setIsTyping(false);
      setIsDisabled(false);
      addMessage(`发生错误: ${error instanceof Error ? error.message : String(error)}`, 'assistant');
    }
  }, [addMessage, addToolCallMessage, clearInput, setIsTyping, setIsDisabled]);

  return {
    ...state,
    addMessage,
    addToolCallMessage,
    setInputValue,
    setIsTyping,
    setIsDisabled,
    clearInput,
    sendMessage
  };
}; 