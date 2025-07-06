import { ChatOllama } from "@langchain/ollama";
import { createReactAgent } from "@langchain/langgraph/prebuilt";
import { tool } from "@langchain/core/tools";
import { z } from "zod";
import { ENV_CONFIG } from "../config/env";

// 简单的测试工具
const testTool = tool(
  async (input: { query: string }) => {
    return `测试工具返回: ${input.query}`;
  },
  {
    name: "test_tool",
    description: "一个简单的测试工具",
    schema: z.object({
      query: z.string().describe("测试查询"),
    }) as any,
  }
);

export async function testOllamaAndAgent() {
  try {
    console.log('开始测试 Ollama 和 LangGraph Agent...');
    
    // 测试基本的 Ollama 模型
    const model = new ChatOllama({
      baseUrl: ENV_CONFIG.OLLAMA_CONFIG.baseUrl,
      model: ENV_CONFIG.OLLAMA_CONFIG.model,
      temperature: 0.7,
    });

    console.log('测试基本模型响应...');
    const basicResponse = await model.invoke("Hello, how are you?");
    console.log('基本模型响应:', basicResponse);

    // 测试 ReAct Agent
    console.log('创建 ReAct Agent...');
    const agent = createReactAgent({
      llm: model,
      tools: [testTool],
    });

    console.log('测试 Agent 响应...');
    const agentResponse = await agent.invoke({
      messages: [{
        role: "user",
        content: "请使用测试工具处理 'hello world'"
      }]
    });

    console.log('Agent 响应:', agentResponse);
    
    return {
      success: true,
      basicResponse,
      agentResponse
    };
  } catch (error) {
    console.error('测试失败:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : String(error)
    };
  }
}

export async function testStreamingAgent() {
  try {
    console.log('开始测试流式 Agent...');
    
    const model = new ChatOllama({
      baseUrl: ENV_CONFIG.OLLAMA_CONFIG.baseUrl,
      model: ENV_CONFIG.OLLAMA_CONFIG.model,
      temperature: 0.7,
    });

    const agent = createReactAgent({
      llm: model,
      tools: [testTool],
    });

    console.log('测试流式 Agent 响应...');
    const stream = await agent.stream({
      messages: [{
        role: "user",
        content: "请简单回答：你好吗？"
      }]
    });

    let finalMessage = "";
    let chunkCount = 0;
    
    for await (const chunk of stream) {
      chunkCount++;
      console.log(`流式 chunk ${chunkCount}:`, chunk);
      
      if (chunk.agent && chunk.agent.messages) {
        const messages = Array.isArray(chunk.agent.messages) ? chunk.agent.messages : [chunk.agent.messages];
        if (messages.length > 0) {
          const lastMessage = messages[messages.length - 1];
          if (typeof lastMessage === 'object' && lastMessage && 'content' in lastMessage && lastMessage.content) {
            finalMessage = String(lastMessage.content);
            console.log('当前消息内容:', finalMessage);
          }
        }
      }
    }

    console.log('流式测试完成，最终消息:', finalMessage);
    
    return {
      success: true,
      finalMessage,
      chunkCount
    };
  } catch (error) {
    console.error('流式测试失败:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : String(error)
    };
  }
}

export async function testOllamaStreaming() {
  try {
    console.log('开始测试 Ollama 流式 API...');
    
    const response = await fetch(`${ENV_CONFIG.OLLAMA_CONFIG.baseUrl}/api/generate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        model: ENV_CONFIG.OLLAMA_CONFIG.model,
        prompt: "请简单回答：你好吗？",
        stream: true,
        options: {
          temperature: 0.7,
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
    let chunkCount = 0;
    const decoder = new TextDecoder();

    console.log('开始读取流式响应...');

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
            chunkCount++;
            finalMessage += data.response;
            console.log(`Chunk ${chunkCount}: "${data.response}"`);
          }
          
          if (data.done) {
            console.log('流式响应完成');
            break;
          }
        } catch (e) {
          console.warn('Failed to parse JSON line:', line);
        }
      }
    }

    console.log('流式测试完成，最终消息:', finalMessage);
    
    return {
      success: true,
      finalMessage,
      chunkCount
    };
  } catch (error) {
    console.error('Ollama 流式测试失败:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : String(error)
    };
  }
} 