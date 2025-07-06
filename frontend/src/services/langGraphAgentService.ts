import { AgentCore } from "./agentCore";
import { StreamHandlers, ToolCallHandler } from "../types/agent";

/**
 * LangGraph Agent Service
 * 
 * 这是一个兼容层，保持原有的静态方法接口，
 * 但内部使用重构后的 AgentCore 类
 */
export class LangGraphAgentService {
  private static agentCore: AgentCore = new AgentCore();

  /**
   * 初始化Agent
   */
  static async initialize(): Promise<void> {
    await this.agentCore.initialize();
  }

  /**
   * 发送消息并处理流式响应
   */
  static async sendMessageWithStreaming(
    message: string, 
    onChunk: (chunk: string) => void,
    onComplete: (finalMessage: string) => void,
    onToolCall?: ToolCallHandler
  ): Promise<void> {
    const handlers: StreamHandlers = {
      onChunk,
      onComplete,
      onToolCall
    };
    
    await this.agentCore.sendMessageWithStreaming(message, handlers);
  }

  /**
   * 测试工具功能
   */
  static async testTools(): Promise<void> {
    await this.agentCore.testTools();
  }
} 