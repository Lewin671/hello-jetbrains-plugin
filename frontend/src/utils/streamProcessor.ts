import { StreamChunk, StreamHandlers, StreamState } from "../types/agent";
import { ToolCallProcessor } from "./toolCallHandler";

// 流式处理器类
export class StreamProcessor {
  private state: StreamState;
  private handlers: StreamHandlers;
  
  constructor(handlers: StreamHandlers) {
    this.handlers = handlers;
    this.state = {
      finalMessage: "",
      chunkCount: 0,
      isCompleted: false
    };
  }
  
  // 处理流式数据块
  async processChunk(chunk: StreamChunk): Promise<void> {
    this.state.chunkCount++;
    
    console.log(`流式 chunk ${this.state.chunkCount}:`, chunk);
    this.logChunkDetails(chunk);
    
    // 处理工具调用块
    await ToolCallProcessor.handleToolChunk(chunk, this.handlers.onToolCall);
    
    // 处理代理消息
    this.processAgentMessages(chunk);
    
    // 处理工具消息
    this.processToolMessages(chunk);
  }
  
  // 记录块的详细信息
  private logChunkDetails(chunk: StreamChunk): void {
    console.log(`🔍 Chunk keys:`, Object.keys(chunk));
    console.log(`🔍 Chunk type:`, typeof chunk);
    
    // 详细检查chunk的每个属性
    for (const [key, value] of Object.entries(chunk)) {
      console.log(`🔍 Key: ${key}, Type: ${typeof value}, Value:`, value);
    }
  }
  
  // 处理代理消息
  private processAgentMessages(chunk: StreamChunk): void {
    if (!chunk.agent?.messages) return;
    
    const messages = Array.isArray(chunk.agent.messages) 
      ? chunk.agent.messages 
      : [chunk.agent.messages];
    
    if (messages.length > 0) {
      const lastMessage = messages[messages.length - 1];
      
      if (this.isValidMessage(lastMessage)) {
        const content = String(lastMessage.content);
        if (content.trim() !== '') {
          this.state.finalMessage = content;
          console.log('当前消息内容:', this.state.finalMessage);
          this.handlers.onChunk(this.state.finalMessage);
        }
      }
    }
  }
  
  // 处理工具消息
  private processToolMessages(chunk: StreamChunk): void {
    if (!chunk.tools?.messages) return;
    
    const toolMessages = Array.isArray(chunk.tools.messages)
      ? chunk.tools.messages
      : [chunk.tools.messages];
    
    console.log(`🔧 Processing ${toolMessages.length} tool messages`);
    
    const toolResult = ToolCallProcessor.processToolMessages(
      toolMessages,
      this.handlers.onChunk,
      this.handlers.onToolCall
    );
    
    if (toolResult) {
      this.state.finalMessage = toolResult;
    }
  }
  
  // 检查消息是否有效
  private isValidMessage(message: any): boolean {
    return typeof message === 'object' && 
           message && 
           'content' in message && 
           message.content;
  }
  
  // 完成处理
  complete(): void {
    if (this.state.isCompleted) return;
    
    this.state.isCompleted = true;
    console.log('Stream completed, final message:', this.state.finalMessage);
    this.handlers.onComplete(this.state.finalMessage);
  }
  
  // 获取当前状态
  getState(): StreamState {
    return { ...this.state };
  }
  
  // 重置状态
  reset(): void {
    this.state = {
      finalMessage: "",
      chunkCount: 0,
      isCompleted: false
    };
  }
} 