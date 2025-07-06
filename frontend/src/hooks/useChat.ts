import { useState, useCallback } from 'react';
import { Message, ChatState } from '../types/chat';
import { CHAT_CONSTANTS } from '../constants/chat';
import { createMessage } from '../utils/chatUtils';

export const useChat = () => {
  const [state, setState] = useState<ChatState>({
    messages: [CHAT_CONSTANTS.INITIAL_MESSAGE],
    inputValue: '',
    isTyping: false,
    isDisabled: false
  });

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

  return {
    ...state,
    addMessage,
    setInputValue,
    setIsTyping,
    setIsDisabled,
    clearInput
  };
}; 