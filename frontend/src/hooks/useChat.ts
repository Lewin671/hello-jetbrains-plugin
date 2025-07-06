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
    let timeline: { type: 'text' | 'tool'; data: string | any; timestamp: string }[] = [];

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

              // 添加到时间线
              timeline.push({
                type: 'text',
                data: assistantMessage,
                timestamp: new Date().toISOString()
              });

              const newMsg = createMessage(assistantMessage, 'assistant');
              newMsg.timeline = [...timeline];
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
              
              // 更新时间线中的最后一个文本项
              const lastTimelineItem = timeline[timeline.length - 1];
              if (lastTimelineItem && lastTimelineItem.type === 'text') {
                lastTimelineItem.data = assistantMessage;
              } else {
                // 如果最后一项不是文本，添加新的文本项
                timeline.push({
                  type: 'text',
                  data: assistantMessage,
                  timestamp: new Date().toISOString()
                });
              }
              
              setState(prev => ({
                ...prev,
                messages: prev.messages.map((msg, index) => 
                  index === assistantMessageIndex 
                    ? { 
                        ...msg, 
                        content: assistantMessage, 
                        toolCalls: toolCalls.length > 0 ? toolCalls : undefined,
                        timeline: [...timeline]
                      }
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
            
            // 添加到时间线
            timeline.push({
              type: 'tool',
              data: toolCallWithTimestamp,
              timestamp: new Date().toISOString()
            });
            
            // 更新助手消息，包含时间线
            setState(prev => ({
              ...prev,
              messages: prev.messages.map((msg, index) => 
                index === assistantMessageIndex 
                  ? { 
                      ...msg, 
                      toolCalls: [...toolCalls],
                      timeline: [...timeline],
                      content: msg.content || assistantMessage
                    }
                  : msg
              )
            }));
            
            console.log('🔧 Tool call added to timeline:', toolCallWithTimestamp);
            console.log('🔧 Current timeline:', timeline);
          },
          onSuccess: (finalMessage: string) => {
            // 流式响应完成
            console.log('onSuccess called with:', finalMessage);
            
            // 确保最终消息包含所有工具调用和时间线
            if (assistantMessageIndex >= 0) {
              // 更新时间线中最后的文本内容
              const lastTimelineItem = timeline[timeline.length - 1];
              if (lastTimelineItem && lastTimelineItem.type === 'text') {
                lastTimelineItem.data = finalMessage || assistantMessage;
              } else if (finalMessage) {
                // 如果最后一项不是文本，且有最终消息，添加新的文本项
                timeline.push({
                  type: 'text',
                  data: finalMessage,
                  timestamp: new Date().toISOString()
                });
              }
              
              setState(prev => ({
                ...prev,
                messages: prev.messages.map((msg, index) => 
                  index === assistantMessageIndex 
                    ? { 
                        ...msg, 
                        content: finalMessage || assistantMessage,
                        toolCalls: toolCalls.length > 0 ? toolCalls : undefined,
                        timeline: timeline.length > 0 ? [...timeline] : undefined
                      }
                    : msg
                )
              }));
            }
            
            setIsTyping(false);
            setIsDisabled(false);
            console.log('Message sent successfully:', finalMessage);
            console.log('Final timeline:', timeline);
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