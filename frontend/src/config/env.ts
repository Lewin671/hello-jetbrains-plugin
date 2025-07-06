// 环境配置
export const ENV_CONFIG = {
  // Ollama 配置
  OLLAMA_CONFIG: {
    baseUrl: process.env.REACT_APP_OLLAMA_BASE_URL || 'http://localhost:11434',
    model: "granite3.3:8b",
    temperature: 0.6,
  },
  
  // 是否启用流式响应
  ENABLE_STREAMING: true,
  
  // 是否启用调试模式
  DEBUG_MODE: process.env.NODE_ENV === 'development',
};

// 验证必要的配置
export const validateConfig = () => {
  // 检查 Ollama 服务是否可用
  if (!ENV_CONFIG.OLLAMA_CONFIG.baseUrl) {
    console.warn('OLLAMA_BASE_URL not found. Please set REACT_APP_OLLAMA_BASE_URL environment variable.');
    return false;
  }
  return true;
}; 