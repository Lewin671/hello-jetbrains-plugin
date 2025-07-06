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
    let toolCalls: { toolName: string; toolInput: any; toolOutput: string; timestamp: string }[] = [];
    let assistantMessageIndex = -1;

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
              // 工具调用消息不作为普通文本消息处理，但如果是第一个chunk，需要创建助手消息
              console.log('🔧 Tool call chunk detected, will be handled by onToolCall callback');
              
              if (isFirstChunk) {
                // 创建一个空的助手消息作为容器
                const newMsg = createMessage('', 'assistant');
                setState(prev => {
                  const newMessages = [...prev.messages, newMsg];
                  assistantMessageIndex = newMessages.length - 1;
                  return { ...prev, messages: newMessages };
                });
                isFirstChunk = false;
                console.log('Created empty assistant message container for tool calls');
              }
              return;
            }

            if (isFirstChunk) {
              // 第一个chunk时创建助手消息
              assistantMessage = chunk;
              console.log('Creating first assistant message:', assistantMessage);

              const newMsg = createMessage(assistantMessage, 'assistant');
              setState(prev => {
                const newMessages = [...prev.messages, newMsg];
                assistantMessageIndex = newMessages.length - 1;
                return { ...prev, messages: newMessages };
              });
              isFirstChunk = false;
            } else {
              // 后续chunk时更新最后一条消息
              assistantMessage += chunk;
              console.log('Updating assistant message:', assistantMessage);
              setState(prev => ({
                ...prev,
                messages: prev.messages.map((msg, index) => 
                  index === assistantMessageIndex 
                    ? { ...msg, content: assistantMessage, toolCalls: toolCalls.length > 0 ? toolCalls : undefined }
                    : msg
                )
              }));
            }
          },
          onToolCall: (toolCall) => {
            console.log('🔧 Tool call detected:', toolCall);
            // 将工具调用添加到数组中，而不是创建新消息
            const toolCallWithTimestamp = {
              ...toolCall,
              timestamp: new Date().toISOString()
            };
            toolCalls.push(toolCallWithTimestamp);
            
            // 更新助手消息，包含所有工具调用
            setState(prev => ({
              ...prev,
              messages: prev.messages.map((msg, index) => 
                index === assistantMessageIndex 
                  ? { 
                      ...msg, 
                      toolCalls: [...toolCalls],
                      content: msg.content || assistantMessage
                    }
                  : msg
              )
            }));
            
            console.log('🔧 Tool call added to assistant message:', toolCallWithTimestamp);
            console.log('🔧 Current assistantMessageIndex:', assistantMessageIndex);
            console.log('🔧 Current toolCalls array:', toolCalls);
          },
          onSuccess: (finalMessage: string) => {
            // 流式响应完成
            console.log('onSuccess called with:', finalMessage);
            
            // 确保最终消息包含所有工具调用
            if (assistantMessageIndex >= 0) {
              setState(prev => ({
                ...prev,
                messages: prev.messages.map((msg, index) => 
                  index === assistantMessageIndex 
                    ? { 
                        ...msg, 
                        content: finalMessage || assistantMessage,
                        toolCalls: toolCalls.length > 0 ? toolCalls : undefined
                      }
                    : msg
                )
              }));
            }
            
            setIsTyping(false);
            setIsDisabled(false);
            console.log('Message sent successfully:', finalMessage);
            console.log('Final toolCalls array:', toolCalls);
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