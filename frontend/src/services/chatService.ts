import { SendMessageCallback } from '../types/chat';
import { LangGraphAgentService } from './langGraphAgentService';

export class ChatService {
  static async sendMessageWithStreaming(
    message: string, 
    callbacks: SendMessageCallback & {
      onChunk?: (chunk: string) => void;
      onToolCall?: (toolCall: { toolName: string; toolInput: any; toolOutput: string }) => void;
    }
  ): Promise<void> {
    try {
      console.log('Sending message with streaming:', message);
      
      // 使用LangGraph Agent的流式响应
      await LangGraphAgentService.sendMessageWithStreaming(
        message,
        callbacks.onChunk || (() => {}),
        callbacks.onSuccess || (() => {}),
        callbacks.onToolCall
      );
    } catch (error) {
      console.error("LangGraph Agent streaming failed:", error);
      
      // 备用逻辑：使用bridge
      if (window.sendMessage) {
        window.sendMessage(
          message,
          callbacks.onSuccess,
          callbacks.onFailure
        );
      } else {
        // 最后的备用逻辑：返回错误信息
        console.warn("Bridge function 'window.sendMessage' not found.");
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