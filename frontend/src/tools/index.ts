import { createSearchTool, executeSearch } from "./search";
import { createCalculatorTool, executeCalculation } from "./calculator";
import { SearchInput, CalculatorInput, ToolResult } from "../types/agent";

// 高阶函数：包装工具，自动插入工具调用过程消息
export function withToolCallMessage<T extends object>(
  toolFn: (input: T) => Promise<string>, 
  toolName: string
) {
  return async (input: T) => {
    const output = await toolFn(input);
    // 构造工具调用过程消息
    return `🔧 使用了 ${toolName} 工具\n输入: ${JSON.stringify(input)}\n输出: ${output}`;
  };
}

// 工具管理器类
export class ToolManager {
  private static tools: Map<string, any> = new Map();
  
  // 初始化所有工具
  static initialize() {
    const searchTool = createSearchTool(withToolCallMessage);
    const calculatorTool = createCalculatorTool(withToolCallMessage);
    
    this.tools.set("search", searchTool);
    this.tools.set("calculator", calculatorTool);
    
    console.log('🔧 Tools initialized:', Array.from(this.tools.keys()));
  }
  
  // 获取所有工具
  static getTools() {
    if (this.tools.size === 0) {
      this.initialize();
    }
    return Array.from(this.tools.values());
  }
  
  // 获取特定工具
  static getTool(name: string) {
    if (this.tools.size === 0) {
      this.initialize();
    }
    return this.tools.get(name);
  }
  
  // 直接执行工具（不通过LangChain包装）
  static async executeTool(toolName: string, input: any): Promise<ToolResult> {
    switch (toolName) {
      case "search":
        return await executeSearch(input as SearchInput);
      case "calculator":
        return await executeCalculation(input as CalculatorInput);
      default:
        return {
          success: false,
          result: `未知的工具: ${toolName}`,
          error: `Unknown tool: ${toolName}`
        };
    }
  }
}

// 导出工具创建函数
export { createSearchTool, createCalculatorTool };
export { executeSearch, executeCalculation }; 