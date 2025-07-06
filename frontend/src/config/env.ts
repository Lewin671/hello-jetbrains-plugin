// 环境配置
export const ENV_CONFIG = {
  // OpenAI 配置
  OPENAI_CONFIG: {
    apiKey: process.env.REACT_APP_OPENAI_API_KEY || '',
    model: process.env.REACT_APP_OPENAI_MODEL || 'gpt-3.5-turbo',
    temperature: 0.6,
    baseUrl: process.env.REACT_APP_OPENAI_BASE_URL || 'https://api.openai.com/v1',
  },
  
  // DeepSeek 配置
  DEEPSEEK_CONFIG: {
    apiKey: process.env.REACT_APP_DEEPSEEK_API_KEY || '',
    model: process.env.REACT_APP_DEEPSEEK_MODEL || 'deepseek-chat',
    temperature: 0.6,
    baseUrl: process.env.REACT_APP_DEEPSEEK_BASE_URL || 'https://api.deepseek.com/v1',
  },
  
  // Ollama 配置 (作为备选)
  OLLAMA_CONFIG: {
    baseUrl: process.env.REACT_APP_OLLAMA_BASE_URL || 'http://localhost:11434',
    model: "granite3.3:8b",
    temperature: 0.6,
  },
  
  // 模型选择
  MODEL_PROVIDER: process.env.REACT_APP_MODEL_PROVIDER || 'deepseek', // 'openai', 'deepseek' 或 'ollama'
  
  // 是否启用流式响应
  ENABLE_STREAMING: true,
  
  // 是否启用调试模式
  DEBUG_MODE: process.env.NODE_ENV === 'development',
};

// 验证必要的配置
export const validateConfig = () => {
  if (ENV_CONFIG.MODEL_PROVIDER === 'openai') {
    // 检查 OpenAI 配置
    if (!ENV_CONFIG.OPENAI_CONFIG.apiKey) {
      console.warn('OPENAI_API_KEY not found. Please set REACT_APP_OPENAI_API_KEY environment variable.');
      return false;
    }
  } else if (ENV_CONFIG.MODEL_PROVIDER === 'deepseek') {
    // 检查 DeepSeek 配置
    if (!ENV_CONFIG.DEEPSEEK_CONFIG.apiKey) {
      console.warn('DEEPSEEK_API_KEY not found. Please set REACT_APP_DEEPSEEK_API_KEY environment variable.');
      return false;
    }
  } else if (ENV_CONFIG.MODEL_PROVIDER === 'ollama') {
    // 检查 Ollama 服务是否可用
    if (!ENV_CONFIG.OLLAMA_CONFIG.baseUrl) {
      console.warn('OLLAMA_BASE_URL not found. Please set REACT_APP_OLLAMA_BASE_URL environment variable.');
      return false;
    }
  }
  return true;
}; 