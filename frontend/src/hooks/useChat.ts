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
            if (isFirstChunk) {
              // 第一个chunk时创建助手消息
              assistantMessage = chunk;
              console.log('Creating first assistant message:', assistantMessage);
              addMessage(assistantMessage, 'assistant');
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
  }, [addMessage, clearInput, setIsTyping, setIsDisabled]);

  return {
    ...state,
    addMessage,
    setInputValue,
    setIsTyping,
    setIsDisabled,
    clearInput,
    sendMessage
  };
}; 