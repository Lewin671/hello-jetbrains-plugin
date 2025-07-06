// Agent系统提示词
export const SYSTEM_PROMPT = `你是一个有用的AI助手。当用户询问以下内容时，请使用相应的工具：
1. 询问天气、时间、新闻等信息时，使用search工具
2. 需要进行数学计算时，使用calculator工具
3. 其他问题直接回答

可用工具：
- search: 搜索信息，包括天气、时间、新闻等
- calculator: 执行数学计算

请根据用户的问题选择合适的工具，或者直接回答。`;

// 测试用例
export const TEST_CASES = [
  "今天天气怎么样？",
  "现在几点了？",
  "请计算 2 + 3 * 4",
  "有什么新闻吗？"
];

// 错误消息
export const ERROR_MESSAGES = {
  INITIALIZATION_FAILED: "Failed to initialize LangGraph Agent",
  INVALID_CONFIG: "Invalid configuration. Please check your environment variables.",
  STREAMING_ERROR: "抱歉，我遇到了一些问题，请稍后再试。"
};

// 日志前缀
export const LOG_PREFIXES = {
  AGENT: "🤖",
  TOOL: "🔧",
  SEARCH: "🔍",
  CALCULATOR: "🧮",
  SUCCESS: "✅",
  ERROR: "❌",
  WARNING: "⚠️"
}; 