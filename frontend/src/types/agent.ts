// Agent 相关类型定义

// 工具调用相关类型
export interface ToolCall {
  toolName: string;
  toolInput: any;
  toolOutput: string;
}

export interface ToolCallHandler {
  (toolCall: ToolCall): void;
}

// 流式处理相关类型
export interface StreamChunk {
  agent?: {
    messages: Array<{
      content: string;
      [key: string]: any;
    }>;
  };
  tools?: {
    messages: Array<{
      content: string;
      kwargs?: {
        name?: string;
        content?: string;
        [key: string]: any;
      };
      [key: string]: any;
    }>;
  };
  tool?: any;
  [key: string]: any;
}

export interface StreamHandlers {
  onChunk: (chunk: string) => void;
  onComplete: (finalMessage: string) => void;
  onToolCall?: ToolCallHandler;
}

// 消息相关类型
export interface Message {
  role: 'system' | 'user' | 'assistant';
  content: string;
}

// 工具定义相关类型
export interface ToolDefinition {
  name: string;
  description: string;
  schema: any;
}

// Agent 配置类型
export interface AgentConfig {
  modelProvider: string;
  systemMessage: string;
  tools: ToolDefinition[];
}

// 工具执行结果类型
export interface ToolResult {
  success: boolean;
  result: string;
  error?: string;
}

// 工具输入类型
export interface SearchInput {
  query: string;
}

export interface CalculatorInput {
  expression: string;
}

// 流式处理状态
export interface StreamState {
  finalMessage: string;
  chunkCount: number;
  isCompleted: boolean;
}

// 工具调用解析结果
export interface ParsedToolCall {
  toolName: string;
  toolInput: any;
  toolOutput: string;
} 