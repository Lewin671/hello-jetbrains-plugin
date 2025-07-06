export interface Message {
  id: string;
  content: string;
  sender: 'user' | 'assistant';
  toolCall?: {
    toolName: string;
    toolInput: any;
    toolOutput: string;
    timestamp: string;
  };
}

export interface ChatState {
  messages: Message[];
  inputValue: string;
  isTyping: boolean;
  isDisabled: boolean;
}

export interface SendMessageCallback {
  onSuccess: (response: string) => void;
  onFailure: (error: string) => void;
}

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