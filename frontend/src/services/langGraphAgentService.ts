import { createReactAgent } from "@langchain/langgraph/prebuilt";
import { ChatOllama } from "@langchain/ollama";
import { ChatOpenAI } from "@langchain/openai";
import { tool } from "@langchain/core/tools";
import { z } from "zod";
import { ENV_CONFIG, validateConfig } from "../config/env";

// 高阶函数：包装工具，自动插入工具调用过程消息
function withToolCallMessage<T extends object>(toolFn: (input: T) => Promise<string>, toolName: string) {
  return async (input: T) => {
    const output = await toolFn(input);
    // 构造工具调用过程消息
    return `🔧 使用了 ${toolName} 工具\n输入: ${JSON.stringify(input)}\n输出: ${output}`;
  };
}

// 定义工具函数 - 使用更清晰的描述和参数
const searchTool = tool(
  withToolCallMessage(
    async (input: { query: string }) => {
      console.log('🔍 Search tool called with query:', input.query);
      
      // 模拟搜索功能
      if (input.query.toLowerCase().includes("天气") || input.query.toLowerCase().includes("weather")) {
        return "今天天气晴朗，温度25度，适合外出活动。";
      }
      if (input.query.toLowerCase().includes("时间") || input.query.toLowerCase().includes("time")) {
        return `当前时间是 ${new Date().toLocaleString('zh-CN')}`;
      }
      if (input.query.toLowerCase().includes("新闻") || input.query.toLowerCase().includes("news")) {
        return "今日新闻：科技发展迅速，AI技术不断创新。";
      }
      return `关于"${input.query}"的信息：这是一个模拟的搜索结果。在实际应用中，这里会调用真实的搜索API。`;
    },
    "search"
  ),
  {
    name: "search",
    description: "搜索信息，包括天气、时间、新闻等。当用户询问天气、时间或需要搜索信息时使用此工具。",
    schema: z.object({
      query: z.string().describe("要搜索的查询内容"),
    }) as any,
  }
);

const calculatorTool = tool(
  withToolCallMessage(
    async (input: { expression: string }) => {
      console.log('🧮 Calculator tool called with expression:', input.expression);
      
      try {
        // 安全地计算数学表达式
        const sanitizedExpression = input.expression.replace(/[^0-9+\-*/().]/g, '');
        
        // 使用 Function 构造函数替代 eval，更安全
        // eslint-disable-next-line no-new-func
        const result = new Function(`return ${sanitizedExpression}`)();
        
        return `计算结果: ${result}`;
      } catch (error) {
        return "计算错误，请检查表达式格式是否正确。";
      }
    },
    "calculator"
  ),
  {
    name: "calculator",
    description: "执行数学计算。当用户需要进行数学运算时使用此工具。",
    schema: z.object({
      expression: z.string().describe("要计算的数学表达式，如：2+3*4"),
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
      let model;
      
      if (ENV_CONFIG.MODEL_PROVIDER === 'openai') {
        // 使用 OpenAI 模型
        model = new ChatOpenAI({
          apiKey: ENV_CONFIG.OPENAI_CONFIG.apiKey,
          model: ENV_CONFIG.OPENAI_CONFIG.model,
          temperature: ENV_CONFIG.OPENAI_CONFIG.temperature,
        });
        console.log('🤖 Creating ReAct agent with OpenAI model:', ENV_CONFIG.OPENAI_CONFIG.model);
      } else if (ENV_CONFIG.MODEL_PROVIDER === 'deepseek') {
        // 使用 DeepSeek 模型 (通过 OpenAI 兼容的 API)
        model = new ChatOpenAI({
          apiKey: ENV_CONFIG.DEEPSEEK_CONFIG.apiKey,
          model: ENV_CONFIG.DEEPSEEK_CONFIG.model,
          temperature: ENV_CONFIG.DEEPSEEK_CONFIG.temperature,
          configuration: {
            baseURL: ENV_CONFIG.DEEPSEEK_CONFIG.baseUrl,
          },
        });
        console.log('🤖 Creating ReAct agent with DeepSeek model:', ENV_CONFIG.DEEPSEEK_CONFIG.model);
      } else {
        // 使用 Ollama 模型
        model = new ChatOllama({
          baseUrl: ENV_CONFIG.OLLAMA_CONFIG.baseUrl,
          model: ENV_CONFIG.OLLAMA_CONFIG.model,
          temperature: ENV_CONFIG.OLLAMA_CONFIG.temperature,
        });
        console.log('🤖 Creating ReAct agent with Ollama model:', ENV_CONFIG.OLLAMA_CONFIG.model);
      }

      // 创建ReAct agent
      this.agent = createReactAgent({
        llm: model,
        tools: [searchTool, calculatorTool],
      });

      console.log('✅ Agent created successfully');
      console.log('🔧 Agent type:', typeof this.agent);
      console.log('🔧 Agent keys:', Object.keys(this.agent));
      console.log('🔧 Agent invoke method:', typeof this.agent.invoke);
      console.log('🔧 Agent stream method:', typeof this.agent.stream);
      // tools 
      console.log('🔧 Agent tools:', this.agent);
      this.isInitialized = true;
      console.log(`✅ LangGraph Agent initialized successfully with ${ENV_CONFIG.MODEL_PROVIDER} model`);
    } catch (error) {
      console.error("❌ Failed to initialize LangGraph Agent:", error);
      throw error;
    }
  }

  static async sendMessageWithStreaming(
    message: string, 
    onChunk: (chunk: string) => void,
    onComplete: (finalMessage: string) => void,
    onToolCall?: (toolCall: { toolName: string; toolInput: any; toolOutput: string }) => void
  ) {
    console.log('LangGraphAgentService.sendMessageWithStreaming called with message:', message);
    
    if (!this.isInitialized) {
      console.log('Agent not initialized, initializing...');
      await this.initialize();
    }

    try {
      console.log('Starting streaming with agent...');
      
      // 使用 LangGraph Agent 的流式功能
      const stream = await this.agent.stream({
        messages: [
          {
            role: "system",
            content: `你是一个有用的AI助手。当用户询问以下内容时，请使用相应的工具：
1. 询问天气、时间、新闻等信息时，使用search工具
2. 需要进行数学计算时，使用calculator工具
3. 其他问题直接回答

可用工具：
- search: 搜索信息，包括天气、时间、新闻等
- calculator: 执行数学计算

请根据用户的问题选择合适的工具，或者直接回答。`
          },
          {
            role: "user",
            content: message
          }
        ]
      });

      let finalMessage = "";
      let chunkCount = 0;
      
      for await (const chunk of stream) {
        chunkCount++;
        console.log(`流式 chunk ${chunkCount}:`, chunk);
        console.log(`🔍 Chunk keys:`, Object.keys(chunk));
        console.log(`🔍 Chunk type:`, typeof chunk);
        
        // 详细检查chunk的每个属性
        for (const [key, value] of Object.entries(chunk)) {
          console.log(`🔍 Key: ${key}, Type: ${typeof value}, Value:`, value);
        }
        
        // 检查是否有工具调用 - 支持多种可能的格式
        if (chunk.tool && Array.isArray(chunk.tool) && chunk.tool.length > 0) {
          console.log('🔧 Found tool array:', chunk.tool);
          for (const toolChunk of chunk.tool) {
            if (toolChunk.tool_name && toolChunk.tool_input) {
              console.log('🔧 Tool call detected:', toolChunk);
              
              // 执行工具调用
              let toolOutput = "";
              if (toolChunk.tool_name === "search") {
                const result = await searchTool.invoke(toolChunk.tool_input);
                toolOutput = typeof result === 'string' ? result : JSON.stringify(result);
              } else if (toolChunk.tool_name === "calculator") {
                const result = await calculatorTool.invoke(toolChunk.tool_input);
                toolOutput = typeof result === 'string' ? result : JSON.stringify(result);
              }
              
              // 通知工具调用
              if (onToolCall) {
                onToolCall({
                  toolName: toolChunk.tool_name,
                  toolInput: toolChunk.tool_input,
                  toolOutput: toolOutput
                });
              }
            }
          }
        }
        
        // 检查其他可能的工具调用格式
        if (chunk.tool && typeof chunk.tool === 'object' && !Array.isArray(chunk.tool)) {
          console.log('🔧 Found single tool object:', chunk.tool);
          const toolChunk = chunk.tool;
          if (toolChunk.tool_name && toolChunk.tool_input) {
            console.log('🔧 Tool call detected (single object):', toolChunk);
            
            // 执行工具调用
            let toolOutput = "";
            if (toolChunk.tool_name === "search") {
              const result = await searchTool.invoke(toolChunk.tool_input);
              toolOutput = typeof result === 'string' ? result : JSON.stringify(result);
            } else if (toolChunk.tool_name === "calculator") {
              const result = await calculatorTool.invoke(toolChunk.tool_input);
              toolOutput = typeof result === 'string' ? result : JSON.stringify(result);
            }
            
            // 通知工具调用
            if (onToolCall) {
              onToolCall({
                toolName: toolChunk.tool_name,
                toolInput: toolChunk.tool_input,
                toolOutput: toolOutput
              });
            }
          }
        }
        
        // 检查其他可能的工具调用字段名
        const possibleToolFields = ['tools', 'tool_calls', 'tool_calls', 'actions'];
        for (const field of possibleToolFields) {
          if (chunk[field]) {
            console.log(`🔧 Found potential tool field '${field}':`, chunk[field]);
          }
        }
        
        if (chunk.agent && chunk.agent.messages) {
          const messages = Array.isArray(chunk.agent.messages) ? chunk.agent.messages : [chunk.agent.messages];
          if (messages.length > 0) {
            const lastMessage = messages[messages.length - 1];
            if (typeof lastMessage === 'object' && lastMessage && 'content' in lastMessage && lastMessage.content) {
              const content = String(lastMessage.content);
              if (content.trim() !== '') {
                finalMessage = content;
                console.log('当前消息内容:', finalMessage);
                onChunk(finalMessage);
              }
            }
          }
        }

        // 处理 tools 消息块 (工具调用返回的消息)
        if (chunk.tools && chunk.tools.messages) {
          const toolMsgs = Array.isArray(chunk.tools.messages) ? chunk.tools.messages : [chunk.tools.messages];
          if (toolMsgs.length > 0) {
            const lastToolMsg = toolMsgs[toolMsgs.length - 1];
            if (typeof lastToolMsg === 'object' && lastToolMsg && 'content' in lastToolMsg && lastToolMsg.content) {
              const tContent = String(lastToolMsg.content);
              if (tContent.trim() !== '') {
                console.log('工具调用消息内容:', tContent);
                finalMessage = tContent; // 更新最终消息，但更重要的是实时回调
                onChunk(tContent);
              }
            }
          }
        }
      }

      console.log('Stream completed, final message:', finalMessage);
      onComplete(finalMessage);
    } catch (error) {
      console.error("Error in LangGraph Agent streaming:", error);
      const errorMessage = "抱歉，我遇到了一些问题，请稍后再试。";
      onChunk(errorMessage);
      onComplete(errorMessage);
    }
  }

  // 测试函数 - 验证tools是否正常工作
  static async testTools(): Promise<void> {
    console.log('🧪 Testing tools functionality...');
    
    if (!this.isInitialized) {
      await this.initialize();
    }

    const testCases = [
      "今天天气怎么样？",
      "现在几点了？",
      "请计算 2 + 3 * 4",
      "有什么新闻吗？"
    ];

    for (const testCase of testCases) {
      console.log(`\n🔍 Testing: "${testCase}"`);
      try {
        let response = "";
        let toolCallCount = 0;
        
        await this.sendMessageWithStreaming(
          testCase,
          (chunk) => { 
            response = chunk; 
            console.log(`📝 Chunk received: ${chunk}`);
          },
          (final) => { 
            response = final; 
            console.log(`✅ Final response: ${final}`);
          },
          (toolCall) => {
            toolCallCount++;
            console.log(`🔧 Tool call ${toolCallCount}:`, toolCall);
          }
        );
        console.log(`✅ Response: ${response}`);
        console.log(`🔧 Tool calls made: ${toolCallCount}`);
      } catch (error) {
        console.error(`❌ Error for "${testCase}":`, error);
      }
    }
  }
} 