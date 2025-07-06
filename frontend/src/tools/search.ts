import { z } from "zod";
import { BaseTool, registerTool } from "./core";

// 搜索输入类型
export interface SearchInput {
  query: string;
}

// 搜索工具实现
@registerTool()
export class SearchTool implements BaseTool<SearchInput> {
  name = "search";
  description = "搜索信息，包括天气、时间、新闻等。当用户询问天气、时间或需要搜索信息时使用此工具。";
  schema = z.object({
    query: z.string().describe("要搜索的查询内容"),
  }).strict();

  async execute(input: SearchInput): Promise<string> {
    console.log('🔍 Search tool executing with query:', input.query);
    
    try {
      const query = input.query.toLowerCase();
      
      // 根据查询类型返回不同的模拟结果
      if (query.includes("天气") || query.includes("weather")) {
        return "今天天气晴朗，温度25度，适合外出活动。";
      } else if (query.includes("时间") || query.includes("time")) {
        return `当前时间是 ${new Date().toLocaleString('zh-CN')}`;
      } else if (query.includes("新闻") || query.includes("news")) {
        return "今日新闻：科技发展迅速，AI技术不断创新。";
      } else {
        return `关于"${input.query}"的信息：这是一个模拟的搜索结果。在实际应用中，这里会调用真实的搜索API。`;
      }
    } catch (error) {
      console.error('Search tool error:', error);
      throw error;
    }
  }
} 