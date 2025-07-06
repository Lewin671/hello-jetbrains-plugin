import { tool } from "@langchain/core/tools";
import { z } from "zod";

// 基础工具接口
export interface BaseTool<T = any> {
  execute(input: T): Promise<string>;
  name: string;
  description: string;
  schema: z.ZodType<T>;
}

// 工具管理器类
export class ToolManager {
  private static tools: Map<string, BaseTool> = new Map();
  private static initialized = false;
  
  // 初始化工具 - 确保所有工具都被导入和注册
  static async initialize(): Promise<void> {
    if (this.initialized) {
      return;
    }
    
    console.log('🔧 Initializing tools...');
    
    // 动态导入工具类以触发装饰器
    try {
      await import('./calculator');
      await import('./search');
      this.initialized = true;
      console.log(`🔧 Tools initialized successfully. Registered ${this.tools.size} tools.`);
    } catch (error) {
      console.error('🔧 Failed to initialize tools:', error);
      throw error;
    }
  }
  
  // 注册工具装饰器
  static register() {
    return function <T extends { new (...args: any[]): BaseTool }>(target: T) {
      const instance = new target();
      ToolManager.registerTool(instance);
      return target;
    };
  }
  
  // 注册工具
  private static registerTool(tool: BaseTool) {
    this.tools.set(tool.name, tool);
    console.log(`🔧 Tool registered: ${tool.name}`);
  }
  
  // 工具调用消息包装器
  private static async withToolCallMessage(tool: BaseTool, input: any): Promise<string> {
    const output = await tool.execute(input);
    return `🔧 使用了 ${tool.name} 工具\n输入: ${JSON.stringify(input)}\n输出: ${output}`;
  }
  
  // 获取所有工具的 LangChain 包装
  static getTools() {
    // 如果还没有初始化，先初始化
    if (!this.initialized) {
      console.warn('🔧 Tools not initialized, initializing synchronously...');
      // 同步导入（仅用于备用）
      try {
        require('./calculator');
        require('./search');
        this.initialized = true;
      } catch (error) {
        console.error('🔧 Failed to synchronously initialize tools:', error);
      }
    }
    
    return Array.from(this.tools.values()).map(toolInstance => 
      tool(
        async (input: any) => this.withToolCallMessage(toolInstance, input),
        {
          name: toolInstance.name,
          description: toolInstance.description,
          schema: toolInstance.schema,
        }
      )
    );
  }
  
  // 直接执行工具
  static async executeTool(toolName: string, input: any): Promise<any> {
    // 确保工具已初始化
    if (!this.initialized) {
      await this.initialize();
    }
    
    const toolInstance = this.tools.get(toolName);
    if (!toolInstance) {
      return {
        success: false,
        result: `未知的工具: ${toolName}`,
        error: `Unknown tool: ${toolName}`
      };
    }
    
    try {
      const result = await toolInstance.execute(input);
      return {
        success: true,
        result
      };
    } catch (error) {
      return {
        success: false,
        result: error instanceof Error ? error.message : "执行出错",
        error: error instanceof Error ? error.message : "Unknown error"
      };
    }
  }
  
  // 获取已注册的工具数量
  static getToolCount(): number {
    return this.tools.size;
  }
  
  // 获取所有工具名称
  static getToolNames(): string[] {
    return Array.from(this.tools.keys());
  }
}

// 导出注册装饰器
export const registerTool = ToolManager.register; 