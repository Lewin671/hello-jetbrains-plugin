import { createReactAgent } from "@langchain/langgraph/prebuilt";
import { ChatOllama } from "@langchain/ollama";
import { tool } from "@langchain/core/tools";
import { z } from "zod";
import { ENV_CONFIG, validateConfig } from "../config/env";

// 定义工具函数
const search = tool(
  async (input: { query: string }) => {
    const { query } = input;
    console.log('Search tool called with query:', query);
    
    // 模拟搜索功能
    if (query.toLowerCase().includes("天气") || query.toLowerCase().includes("weather")) {
      return "今天天气晴朗，温度25度，适合外出活动。";
    }
    if (query.toLowerCase().includes("时间") || query.toLowerCase().includes("time")) {
      return `当前时间是 ${new Date().toLocaleString('zh-CN')}`;
    }
    if (query.toLowerCase().includes("计算") || query.toLowerCase().includes("calculate")) {
      // 简单的数学计算
      try {
        const result = eval(query.replace(/[^0-9+\-*/().]/g, ''));
        return `计算结果: ${result}`;
      } catch (error) {
        return "无法计算该表达式";
      }
    }
    return `关于"${query}"的信息：这是一个模拟的搜索结果。在实际应用中，这里会调用真实的搜索API。`;
  },
  {
    name: "search",
    description: "搜索信息或进行计算",
    schema: z.object({
      query: z.string().describe("要搜索的查询内容或要计算的表达式"),
    }) as any,
  }
);

const calculator = tool(
  async (input: { expression: string }) => {
    const { expression } = input;
    console.log('Calculator tool called with expression:', expression);
    
    try {
      // 安全地计算数学表达式
      const sanitizedExpression = expression.replace(/[^0-9+\-*/().]/g, '');
      const result = eval(sanitizedExpression);
      return `计算结果: ${result}`;
    } catch (error) {
      return "计算错误，请检查表达式";
    }
  },
  {
    name: "calculator",
    description: "执行数学计算",
    schema: z.object({
      expression: z.string().describe("要计算的数学表达式"),
    }) as any,
  }
);

// 创建LangGraph Agent
export class LangGraphAgentService {
  private static agent: any = null;
  private static isInitialized = false;

  static async initialize() {
    if (this.isInitialized) {
      return;
    }

    // 验证配置
    if (!validateConfig()) {
      throw new Error("Invalid configuration. Please check your environment variables.");
    }

    try {
      // 使用 Ollama granite3.3:8b 模型
      const model = new ChatOllama({
        baseUrl: ENV_CONFIG.OLLAMA_CONFIG.baseUrl,
        model: ENV_CONFIG.OLLAMA_CONFIG.model,
        temperature: ENV_CONFIG.OLLAMA_CONFIG.temperature,
      });

      console.log('Creating ReAct agent with model:', ENV_CONFIG.OLLAMA_CONFIG.model);

      // 创建ReAct agent
      this.agent = createReactAgent({
        llm: model,
        tools: [search, calculator],
      });

      this.isInitialized = true;
      console.log("LangGraph Agent initialized successfully with Ollama model");
    } catch (error) {
      console.error("Failed to initialize LangGraph Agent:", error);
      throw error;
    }
  }

  static async sendMessage(message: string): Promise<string> {
    if (!this.isInitialized) {
      await this.initialize();
    }

    try {
      console.log('Sending message to agent:', message);
      const result = await this.agent.invoke({
        messages: [{
          role: "user",
          content: message
        }]
      });

      console.log('Agent result:', result);

      // 提取最后一条消息作为回复
      if (result.messages && result.messages.length > 0) {
        const lastMessage = result.messages[result.messages.length - 1];
        console.log('Last message from agent:', lastMessage);
        
        if (lastMessage.content && lastMessage.content.trim() !== '') {
          return lastMessage.content;
        } else {
          console.warn('Last message has no content, checking for tool results');
          // 如果没有内容，可能是工具调用的结果
          // 尝试从其他消息中获取内容
          for (let i = result.messages.length - 1; i >= 0; i--) {
            const msg = result.messages[i];
            if (msg.content && msg.content.trim() !== '') {
              return msg.content;
            }
          }
        }
      }
      
      return "抱歉，我没有收到有效的回复。";
    } catch (error) {
      console.error("Error in LangGraph Agent:", error);
      return "抱歉，我遇到了一些问题，请稍后再试。";
    }
  }

  static async sendMessageWithStreaming(
    message: string, 
    onChunk: (chunk: string) => void,
    onComplete: (finalMessage: string) => void
  ) {
    console.log('LangGraphAgentService.sendMessageWithStreaming called with message:', message);
    
    if (!this.isInitialized) {
      console.log('Agent not initialized, initializing...');
      await this.initialize();
    }

    // 如果不启用流式响应，使用普通模式
    if (!ENV_CONFIG.ENABLE_STREAMING) {
      console.log('Streaming disabled, using regular mode');
      const response = await this.sendMessage(message);
      onChunk(response);
      onComplete(response);
      return;
    }

    try {
      console.log('Starting streaming with agent...');
      
      // 使用Ollama的直接流式API
      const response = await fetch(`${ENV_CONFIG.OLLAMA_CONFIG.baseUrl}/api/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model: ENV_CONFIG.OLLAMA_CONFIG.model,
          prompt: message,
          stream: true,
          options: {
            temperature: ENV_CONFIG.OLLAMA_CONFIG.temperature,
          }
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('No reader available');
      }

      let finalMessage = "";
      const decoder = new TextDecoder();

      while (true) {
        const { done, value } = await reader.read();
        
        if (done) {
          break;
        }

        const chunk = decoder.decode(value);
        const lines = chunk.split('\n').filter(line => line.trim() !== '');

        for (const line of lines) {
          try {
            const data = JSON.parse(line);
            
            if (data.response) {
              finalMessage += data.response;
              console.log('Streaming chunk:', data.response);
              onChunk(data.response);
            }
            
            if (data.done) {
              console.log('Stream completed');
              break;
            }
          } catch (e) {
            console.warn('Failed to parse JSON line:', line);
          }
        }
      }

      console.log('Stream completed, final message:', finalMessage);
      onComplete(finalMessage);
    } catch (error) {
      console.error("Error in LangGraph Agent streaming:", error);
      
      // 如果流式失败，回退到普通模式
      try {
        console.log('Falling back to regular mode...');
        const response = await this.sendMessage(message);
        onChunk(response);
        onComplete(response);
      } catch (fallbackError) {
        console.error("Fallback also failed:", fallbackError);
        const errorMessage = "抱歉，我遇到了一些问题，请稍后再试。";
        onChunk(errorMessage);
        onComplete(errorMessage);
      }
    }
  }
} 