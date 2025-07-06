import { tool } from "@langchain/core/tools";
import { z } from "zod";
import { CalculatorInput, ToolResult } from "../types/agent";

// 计算器工具的核心逻辑
export async function executeCalculation(input: CalculatorInput): Promise<ToolResult> {
  console.log('🧮 Calculator tool executing with expression:', input.expression);
  
  try {
    // 安全地计算数学表达式
    const sanitizedExpression = input.expression.replace(/[^0-9+\-*/().]/g, '');
    
    if (!sanitizedExpression) {
      return {
        success: false,
        result: "无效的数学表达式，请输入正确的计算公式。",
        error: "Invalid expression"
      };
    }
    
    // 使用 Function 构造函数替代 eval，更安全
    // eslint-disable-next-line no-new-func
    const calculationResult = new Function(`return ${sanitizedExpression}`)();
    
    if (typeof calculationResult !== 'number' || !isFinite(calculationResult)) {
      return {
        success: false,
        result: "计算结果无效，请检查表达式是否正确。",
        error: "Invalid calculation result"
      };
    }
    
    return {
      success: true,
      result: `计算结果: ${calculationResult}`
    };
  } catch (error) {
    console.error('Calculator tool error:', error);
    return {
      success: false,
      result: "计算错误，请检查表达式格式是否正确。",
      error: error instanceof Error ? error.message : 'Unknown error'
    };
  }
}

// 创建计算器工具包装器
export function createCalculatorTool(withToolCallMessage: (toolFn: any, toolName: string) => any) {
  return tool(
    withToolCallMessage(
      async (input: CalculatorInput) => {
        const result = await executeCalculation(input);
        return result.result;
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
} 