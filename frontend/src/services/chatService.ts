import { SendMessageCallback } from '../types/chat';
import { CHAT_CONSTANTS } from '../constants/chat';

export class ChatService {
  static sendMessage(message: string, callbacks: SendMessageCallback): void {
    if (window.sendMessage) {
      window.sendMessage(
        message,
        callbacks.onSuccess,
        callbacks.onFailure
      );
    } else {
      // 备用逻辑：模拟网络延迟
      console.warn("Bridge function 'window.sendMessage' not found. Running in fallback mode.");
      setTimeout(() => {
        callbacks.onFailure(CHAT_CONSTANTS.FALLBACK_ERROR);
      }, 1000);
    }
  }
} 