# AI 助手 React 前端

这是一个基于 React 和 TypeScript 的 AI 聊天界面，专为 IntelliJ IDEA 插件设计。

## 功能特性

- 🤖 现代化的聊天界面设计
- 🌓 自动适应 IDE 的亮色/暗色主题
- 💬 实时消息交互
- ⌨️ 支持回车键发送消息
- 🔄 输入状态指示器
- 📱 响应式布局设计
- 🎨 自定义滚动条样式
- 🚀 支持 Ollama 本地模型
- 🔧 **工具调用可视化** - 显示AI使用的工具和调用结果

## 工具调用功能

该应用支持 LangGraph Agent 的工具调用功能，当AI使用工具时，会在对话中显示：

- **工具名称** - 显示使用的工具类型（如 search、calculator）
- **工具输入** - 显示传递给工具的参数
- **工具输出** - 显示工具的返回结果
- **时间戳** - 记录工具调用的时间

### 支持的工具

1. **搜索工具 (search)** - 查询天气、时间、新闻等信息
2. **计算器工具 (calculator)** - 执行数学计算

### 使用方法

1. 在聊天界面输入会触发工具使用的问题，例如：
   - "今天天气怎么样？"
   - "请计算 2 + 3 * 4"
   - "现在几点了？"
   - "有什么新闻吗？"

2. AI会自动选择合适的工具并执行调用

3. 工具调用信息会以特殊格式显示在对话中，包含：
   - 🔧 工具图标
   - 工具名称（大写显示）
   - 输入参数（JSON格式）
   - 输出结果

### 演示

点击界面上的"演示工具调用"按钮可以快速测试工具调用功能。

## 技术栈

- **React 19** - 现代化的 React 框架
- **TypeScript** - 类型安全的 JavaScript
- **CSS Variables** - 主题系统支持
- **JCEF Bridge** - 与 Kotlin 后端的通信
- **LangChain** - AI 模型集成
- **LangGraph** - 工具调用和Agent框架
- **Ollama** - 本地大语言模型

## Ollama 配置

本项目使用 Ollama 作为 AI 模型后端，支持本地运行的大语言模型。

### 安装 Ollama

1. 访问 [Ollama 官网](https://ollama.ai/) 下载并安装
2. 启动 Ollama 服务

### 下载模型

```bash
# 下载 granite3.3:8b 模型
ollama pull granite3.3:8b
```

### 环境变量配置

创建 `.env` 文件并设置：

```env
# Ollama 服务地址 (默认: http://localhost:11434)
REACT_APP_OLLAMA_BASE_URL=http://localhost:11434
```

### 验证连接

启动应用后，可以在浏览器控制台运行：

```javascript
import { testOllamaConnection } from './src/services/ollamaTest';
testOllamaConnection();
```

## 主题支持

该界面使用 CSS 变量系统，能够自动适应 IntelliJ IDEA 的主题：

- **亮色主题**: 使用浅色背景和深色文字
- **暗色主题 (Darcula)**: 自动切换到深色背景和浅色文字
- **备用值**: 在普通浏览器中提供合理的默认样式

## 组件结构

```
App.tsx
├── Chat Container (主容器)
├── Chat Header (标题栏)
├── Chat Messages (消息区域)
│   ├── Welcome Message (欢迎消息)
│   ├── User Messages (用户消息)
│   └── Assistant Messages (AI 消息)
├── Typing Indicator (输入指示器)
└── Chat Input (输入区域)
    ├── Message Input (文本输入框)
    └── Send Button (发送按钮)
```

## 与后端通信

界面通过 `window.sendMessage` 函数与 Kotlin 后端进行通信：

```typescript
window.sendMessage(
  message: string,
  onSuccess: (response: string) => void,
  onFailure: (error: string) => void
)
```

## 开发

```bash
# 安装依赖
npm install

# 启动开发服务器
npm start

# 构建生产版本
npm run build

# 运行测试
npm test
```

## 样式系统

样式文件分为两部分：

- `index.css` - 全局样式和主题变量
- `App.css` - 组件特定样式

所有样式都使用 CSS 变量，确保与 IDE 主题的一致性。

## 浏览器兼容性

- Chrome (推荐)
- Firefox
- Safari
- Edge

## 注意事项

- 该界面设计为在 IntelliJ IDEA 的 JCEF 环境中运行
- 支持键盘快捷键 (Enter 发送消息)
- 自动滚动到最新消息
- 防止重复发送消息
