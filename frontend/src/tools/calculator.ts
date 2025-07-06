import { z } from "zod";
import { BaseTool, registerTool } from "./core";

// 计算器输入类型
export interface CalculatorInput {
  expression: string;
}

// 计算器工具实现
@registerTool()
export class CalculatorTool implements BaseTool<CalculatorInput> {
  name = "calculator";
  description = "执行数学计算。当用户需要进行数学运算时使用此工具。";
  schema = z.object({
    expression: z.string().describe("要计算的数学表达式，如：2+3*4"),
  }).strict();

  async execute(input: CalculatorInput): Promise<string> {
    console.log('🧮 Calculator tool executing with expression:', input.expression);
    
    try {
      // 安全地计算数学表达式
      const sanitizedExpression = input.expression.replace(/[^0-9+\-*/().]/g, '');
      
      if (!sanitizedExpression) {
        throw new Error("无效的数学表达式，请输入正确的计算公式。");
      }
      
      // 使用 Function 构造函数替代 eval，更安全
      // eslint-disable-next-line no-new-func
      const calculationResult = new Function(`return ${sanitizedExpression}`)();
      
      if (typeof calculationResult !== 'number' || !isFinite(calculationResult)) {
        throw new Error("计算结果无效，请检查表达式是否正确。");
      }
      
      return `计算结果: ${calculationResult}`;
    } catch (error) {
      console.error('Calculator tool error:', error);
      throw error;
    }
  }
} 