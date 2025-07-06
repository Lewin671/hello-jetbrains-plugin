import { Message } from '../types/chat';

export const createMessage = (content: string, sender: 'user' | 'assistant'): Message => ({
  id: Date.now().toString(),
  content,
  sender
});

export const isValidMessage = (message: string): boolean => {
  return message.trim().length > 0;
};

export const focusInput = (inputRef: React.RefObject<HTMLInputElement | null>): void => {
  inputRef.current?.focus();
}; 