import { SendMessageCallback } from '../types/chat';
import { CHAT_CONSTANTS } from '../constants/chat';
import { LangGraphAgentService } from './langGraphAgentService';

export class ChatService {
  static async sendMessage(message: string, callbacks: SendMessageCallback): Promise<void> {
    try {
      // 优先使用LangGraph Agent
      const response = await LangGraphAgentService.sendMessage(message);
      callbacks.onSuccess(response);
    } catch (error) {
      console.warn("LangGraph Agent failed, falling back to bridge:", error);
      
      // 备用逻辑：使用原有的bridge
      if (window.sendMessage) {
        window.sendMessage(
          message,
          callbacks.onSuccess,
          callbacks.onFailure
        );
      } else {
        // 最后的备用逻辑：模拟网络延迟
        console.warn("Bridge function 'window.sendMessage' not found. Running in fallback mode.");
        setTimeout(() => {
          callbacks.onFailure(CHAT_CONSTANTS.FALLBACK_ERROR);
        }, 1000);
      }
    }
  }

  static async sendMessageWithStreaming(
    message: string, 
    callbacks: SendMessageCallback & {
      onChunk?: (chunk: string) => void;
    }
  ): Promise<void> {
    try {
      console.log('Sending message with streaming:', message);
      
      // 使用LangGraph Agent的流式响应
      await LangGraphAgentService.sendMessageWithStreaming(
        message,
        callbacks.onChunk || (() => {}),
        callbacks.onSuccess || (() => {})
      );
    } catch (error) {
      console.error("LangGraph Agent streaming failed:", error);
      
      // 备用逻辑：使用非流式模式
      try {
        await this.sendMessage(message, callbacks);
      } catch (fallbackError) {
        console.error("Fallback also failed:", fallbackError);
        if (callbacks.onFailure) {
          callbacks.onFailure(`发送消息失败: ${error instanceof Error ? error.message : String(error)}`);
        }
      }
    }
  }

  // 初始化LangGraph Agent
  static async initializeAgent(): Promise<void> {
    try {
      await LangGraphAgentService.initialize();
    } catch (error) {
      console.error("Failed to initialize LangGraph Agent:", error);
    }
  }
} 