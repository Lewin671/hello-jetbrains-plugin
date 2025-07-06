import { createReactAgent } from "@langchain/langgraph/prebuilt";
import { ChatOllama } from "@langchain/ollama";
import { ChatOpenAI } from "@langchain/openai";
import { ENV_CONFIG, validateConfig } from "../config/env";
import { ToolManager } from "../tools";
import { StreamProcessor } from "../utils/streamProcessor";
import { StreamHandlers, Message } from "../types/agent";
import { SYSTEM_PROMPT, ERROR_MESSAGES, LOG_PREFIXES, TEST_CASES } from "../constants/agent";

// Agent核心服务类
export class AgentCore {
  private agent: any = null;
  private isInitialized = false;
  
  // 初始化Agent
  async initialize(): Promise<void> {
    if (this.isInitialized) {
      return;
    }

    // 验证配置
    if (!validateConfig()) {
      throw new Error(ERROR_MESSAGES.INVALID_CONFIG);
    }

    try {
      // 先初始化工具
      await ToolManager.initialize();
      console.log(`${LOG_PREFIXES.TOOL} Tools initialized: ${ToolManager.getToolNames().join(', ')}`);
      
      const model = this.createModel();
      
      // 创建ReAct agent
      this.agent = createReactAgent({
        llm: model,
        tools: ToolManager.getTools(),
      });

      this.logAgentInfo();
      this.isInitialized = true;
      
      console.log(
        `${LOG_PREFIXES.SUCCESS} LangGraph Agent initialized successfully with ${ENV_CONFIG.MODEL_PROVIDER} model`
      );
    } catch (error) {
      console.error(`${LOG_PREFIXES.ERROR} ${ERROR_MESSAGES.INITIALIZATION_FAILED}:`, error);
      throw error;
    }
  }
  
  // 创建模型实例
  private createModel(): ChatOllama | ChatOpenAI {
    switch (ENV_CONFIG.MODEL_PROVIDER) {
      case 'openai':
        console.log(
          `${LOG_PREFIXES.AGENT} Creating ReAct agent with OpenAI model:`,
          ENV_CONFIG.OPENAI_CONFIG.model
        );
        return new ChatOpenAI({
          apiKey: ENV_CONFIG.OPENAI_CONFIG.apiKey,
          model: ENV_CONFIG.OPENAI_CONFIG.model,
          temperature: ENV_CONFIG.OPENAI_CONFIG.temperature,
        });
        
      case 'deepseek':
        console.log(
          `${LOG_PREFIXES.AGENT} Creating ReAct agent with DeepSeek model:`,
          ENV_CONFIG.DEEPSEEK_CONFIG.model
        );
        return new ChatOpenAI({
          apiKey: ENV_CONFIG.DEEPSEEK_CONFIG.apiKey,
          model: ENV_CONFIG.DEEPSEEK_CONFIG.model,
          temperature: ENV_CONFIG.DEEPSEEK_CONFIG.temperature,
          configuration: {
            baseURL: ENV_CONFIG.DEEPSEEK_CONFIG.baseUrl,
          },
        });
        
      default:
        console.log(
          `${LOG_PREFIXES.AGENT} Creating ReAct agent with Ollama model:`,
          ENV_CONFIG.OLLAMA_CONFIG.model
        );
        return new ChatOllama({
          baseUrl: ENV_CONFIG.OLLAMA_CONFIG.baseUrl,
          model: ENV_CONFIG.OLLAMA_CONFIG.model,
          temperature: ENV_CONFIG.OLLAMA_CONFIG.temperature,
        });
    }
  }
  
  // 记录Agent信息
  private logAgentInfo(): void {
    console.log(`${LOG_PREFIXES.SUCCESS} Agent created successfully`);
    console.log(`${LOG_PREFIXES.TOOL} Agent type:`, typeof this.agent);
    console.log(`${LOG_PREFIXES.TOOL} Agent keys:`, Object.keys(this.agent));
    console.log(`${LOG_PREFIXES.TOOL} Agent invoke method:`, typeof this.agent.invoke);
    console.log(`${LOG_PREFIXES.TOOL} Agent stream method:`, typeof this.agent.stream);
  }
  
  // 流式发送消息
  async sendMessageWithStreaming(message: string, handlers: StreamHandlers): Promise<void> {
    console.log('AgentCore.sendMessageWithStreaming called with message:', message);
    
    if (!this.isInitialized) {
      console.log('Agent not initialized, initializing...');
      await this.initialize();
    }

    try {
      console.log('Starting streaming with agent...');
      
      const messages = this.createMessages(message);
      const stream = await this.agent.stream({ messages });
      
      const processor = new StreamProcessor(handlers);
      
      // 处理流式数据
      for await (const chunk of stream) {
        await processor.processChunk(chunk);
      }
      
      processor.complete();
    } catch (error) {
      console.error(`${LOG_PREFIXES.ERROR} Error in LangGraph Agent streaming:`, error);
      handlers.onChunk(ERROR_MESSAGES.STREAMING_ERROR);
      handlers.onComplete(ERROR_MESSAGES.STREAMING_ERROR);
    }
  }
  
  // 创建消息数组
  private createMessages(userMessage: string): Message[] {
    return [
      {
        role: "system",
        content: SYSTEM_PROMPT
      },
      {
        role: "user",
        content: userMessage
      }
    ];
  }
  
  // 测试工具功能
  async testTools(): Promise<void> {
    console.log(`${LOG_PREFIXES.TOOL} Testing tools functionality...`);
    
    if (!this.isInitialized) {
      await this.initialize();
    }

    for (const testCase of TEST_CASES) {
      console.log(`\n${LOG_PREFIXES.SEARCH} Testing: "${testCase}"`);
      
      try {
        let response = "";
        let toolCallCount = 0;
        
        await this.sendMessageWithStreaming(testCase, {
          onChunk: (chunk) => { 
            response = chunk; 
            console.log(`📝 Chunk received: ${chunk}`);
          },
          onComplete: (final) => { 
            response = final; 
            console.log(`${LOG_PREFIXES.SUCCESS} Final response: ${final}`);
          },
          onToolCall: (toolCall) => {
            toolCallCount++;
            console.log(`${LOG_PREFIXES.TOOL} Tool call ${toolCallCount}:`, toolCall);
          }
        });
        
        console.log(`${LOG_PREFIXES.SUCCESS} Response: ${response}`);
        console.log(`${LOG_PREFIXES.TOOL} Tool calls made: ${toolCallCount}`);
      } catch (error) {
        console.error(`${LOG_PREFIXES.ERROR} Error for "${testCase}":`, error);
      }
    }
  }
  
  // 获取初始化状态
  isAgentInitialized(): boolean {
    return this.isInitialized;
  }
  
  // 重置Agent
  async reset(): Promise<void> {
    this.agent = null;
    this.isInitialized = false;
    console.log(`${LOG_PREFIXES.SUCCESS} Agent reset successfully`);
  }
} 