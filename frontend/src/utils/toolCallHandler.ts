import { ToolCall, ToolCallHandler, ParsedToolCall } from "../types/agent";
import { ToolManager } from "../tools";

// 工具调用处理器类
export class ToolCallProcessor {
  private static readonly TOOL_PREFIX = '🔧 使用了 ';
  
  // 从工具调用消息中解析工具信息
  static parseToolCallFromMessage(content: string, toolMsg?: any): ParsedToolCall | null {
    try {
      let toolName = 'unknown';
      let toolContent = content;
      
      // 从toolMsg.kwargs获取工具名称
      if (toolMsg?.kwargs?.name) {
        toolName = toolMsg.kwargs.name;
      }
      
      // 从toolMsg.kwargs获取内容
      if (toolMsg?.kwargs?.content) {
        toolContent = toolMsg.kwargs.content;
      }
      
      // 如果还是unknown，尝试从内容中解析工具名称
      if (toolName === 'unknown' && toolContent.startsWith(this.TOOL_PREFIX)) {
        const firstLine = toolContent.split('\n')[0];
        toolName = firstLine.replace(this.TOOL_PREFIX, '').replace(' 工具', '').trim();
      }
      
      // 解析工具调用内容，提取输入和输出
      const lines = toolContent.split('\n');
      const inputLine = lines.find(l => l.startsWith('输入:')) || '';
      const outputLine = lines.find(l => l.startsWith('输出:')) || '';
      
      const toolInputStr = inputLine.replace('输入:', '').trim();
      const toolOutput = outputLine.replace('输出:', '').trim();
      
      let toolInput: any = toolInputStr;
      try {
        toolInput = JSON.parse(toolInputStr);
      } catch {
        // 如果不是JSON，保持原始字符串
      }
      
      return {
        toolName,
        toolInput,
        toolOutput
      };
    } catch (error) {
      console.warn('Failed to parse tool call info:', error);
      return null;
    }
  }
  
  // 处理工具调用块（chunk中的工具调用）
  static async handleToolChunk(
    chunk: any,
    onToolCall?: ToolCallHandler
  ): Promise<void> {
    if (!onToolCall) return;
    
    try {
      // 检查是否有工具调用 - 支持多种可能的格式
      if (chunk.tool && Array.isArray(chunk.tool) && chunk.tool.length > 0) {
        console.log('🔧 Found tool array:', chunk.tool);
        for (const toolChunk of chunk.tool) {
          await this.processToolChunk(toolChunk, onToolCall);
        }
      } else if (chunk.tool && typeof chunk.tool === 'object' && !Array.isArray(chunk.tool)) {
        console.log('🔧 Found single tool object:', chunk.tool);
        await this.processToolChunk(chunk.tool, onToolCall);
      }
      
      // 检查其他可能的工具调用字段名
      const possibleToolFields = ['tools', 'tool_calls', 'actions'];
      for (const field of possibleToolFields) {
        if (chunk[field]) {
          console.log(`🔧 Found potential tool field '${field}':`, chunk[field]);
          // 这里可以添加更多的处理逻辑
        }
      }
    } catch (error) {
      console.error('Error handling tool chunk:', error);
    }
  }
  
  // 处理单个工具块
  private static async processToolChunk(
    toolChunk: any,
    onToolCall: ToolCallHandler
  ): Promise<void> {
    if (toolChunk.tool_name && toolChunk.tool_input) {
      console.log('🔧 Tool call detected:', toolChunk);
      
      // 执行工具调用
      const toolResult = await ToolManager.executeTool(
        toolChunk.tool_name,
        toolChunk.tool_input
      );
      
      // 通知工具调用
      onToolCall({
        toolName: toolChunk.tool_name,
        toolInput: toolChunk.tool_input,
        toolOutput: toolResult.result
      });
    }
  }
  
  // 处理工具消息块
  static processToolMessages(
    toolMessages: any[],
    onChunk: (chunk: string) => void,
    onToolCall?: ToolCallHandler
  ): string {
    let finalMessage = "";
    
    // 处理所有工具消息
    for (let i = 0; i < toolMessages.length; i++) {
      const toolMsg = toolMessages[i];
      
      if (typeof toolMsg === 'object' && toolMsg && 'content' in toolMsg && toolMsg.content) {
        const content = String(toolMsg.content);
        
        if (content.trim() !== '') {
          console.log(`工具调用消息内容 ${i + 1}/${toolMessages.length}:`, content);
          finalMessage = content;
          onChunk(content);
          
          // 解析并通知工具调用
          if (onToolCall) {
            const parsedToolCall = this.parseToolCallFromMessage(content, toolMsg);
            if (parsedToolCall) {
              onToolCall(parsedToolCall);
              console.log(`🔧 Tool call callback triggered for ${parsedToolCall.toolName}`, parsedToolCall);
            }
          }
        }
      }
    }
    
    return finalMessage;
  }
} 