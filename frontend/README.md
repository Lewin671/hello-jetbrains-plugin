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

## 技术栈

- **React 19** - 现代化的 React 框架
- **TypeScript** - 类型安全的 JavaScript
- **CSS Variables** - 主题系统支持
- **JCEF Bridge** - 与 Kotlin 后端的通信

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
