export const CHAT_CONSTANTS = {
  INITIAL_MESSAGE: {
    id: 'welcome',
    content: '你好！我是你的AI助手，有什么可以帮助你的吗？',
    sender: 'assistant' as const
  },
  TYPING_TEXT: 'AI 正在思考...',
  INPUT_PLACEHOLDER: '输入你的消息...',
  SEND_BUTTON_TEXT: '发送',
  HEADER_TEXT: '🤖 AI 助手',
  ERROR_PREFIX: '【错误】',
  FALLBACK_ERROR: '与后端的连接似乎已断开。'
} as const; 