import { tool } from "@langchain/core/tools";
import { z } from "zod";
import { SearchInput, ToolResult } from "../types/agent";

// 搜索工具的核心逻辑
export async function executeSearch(input: SearchInput): Promise<ToolResult> {
  console.log('🔍 Search tool executing with query:', input.query);
  
  try {
    const query = input.query.toLowerCase();
    let result: string;
    
    // 根据查询类型返回不同的模拟结果
    if (query.includes("天气") || query.includes("weather")) {
      result = "今天天气晴朗，温度25度，适合外出活动。";
    } else if (query.includes("时间") || query.includes("time")) {
      result = `当前时间是 ${new Date().toLocaleString('zh-CN')}`;
    } else if (query.includes("新闻") || query.includes("news")) {
      result = "今日新闻：科技发展迅速，AI技术不断创新。";
    } else {
      result = `关于"${input.query}"的信息：这是一个模拟的搜索结果。在实际应用中，这里会调用真实的搜索API。`;
    }
    
    return {
      success: true,
      result: result
    };
  } catch (error) {
    console.error('Search tool error:', error);
    return {
      success: false,
      result: "搜索功能暂时不可用，请稍后再试。",
      error: error instanceof Error ? error.message : 'Unknown error'
    };
  }
}

// 创建搜索工具包装器
export function createSearchTool(withToolCallMessage: (toolFn: any, toolName: string) => any) {
  return tool(
    withToolCallMessage(
      async (input: SearchInput) => {
        const result = await executeSearch(input);
        return result.result;
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
} 