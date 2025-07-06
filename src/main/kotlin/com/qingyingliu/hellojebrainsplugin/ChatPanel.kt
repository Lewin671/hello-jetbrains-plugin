package com.qingyingliu.hellojebrainsplugin

import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.*

class ChatPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val chatService = ChatService(project)
    private val browser = JBCefBrowser()
    private val jsQuery = JBCefJSQuery.create(browser)

    init {
        setupUI()
        setupJavaScriptBridge()
        loadChatInterface()
    }

    private fun setupUI() {
        browser.component.preferredSize = java.awt.Dimension(400, 500)
        add(browser.component, BorderLayout.CENTER)
        border = JBUI.Borders.empty(5)
    }

    private fun setupJavaScriptBridge() {
        // 注册 Java 端的 handler
        jsQuery.addHandler { message ->
            return@addHandler try {
                val response = chatService.processMessage(message)
                JBCefJSQuery.Response(response)
            } catch (e: Exception) {
                e.printStackTrace()
                JBCefJSQuery.Response("抱歉，处理消息时出现错误: ${e.message}")
            }
        }

        // 在页面里注入 sendMessage 函数，支持带回调
        browser.cefBrowser.executeJavaScript(
            """
            window.sendMessage = function(message, onSuccess, onFailure) {
                ${jsQuery.inject("message", "onSuccess", "onFailure")}
            };
            """.trimIndent(),
            browser.cefBrowser.url, 0
        )
    }

    private fun loadChatInterface() {
        val htmlContent = """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <title>AI助手</title>
                <style>
                    /* --- 全局和主题变量 --- */
                    :root {
                        /* 默认值 (亮色主题) */
                        --bg-color: var(--jb-background-color, #ffffff);
                        --text-color: var(--jb-text-color, #222222);
                        --border-color: var(--jb-border-color, #d0d0d0);
                        --input-bg-color: var(--jb-editor-background-color, #ffffff);
                        --button-bg-color: var(--jb-button-background-color, #f0f0f0);
                        --button-text-color: var(--jb-button-foreground-color, #222222);
                        --button-hover-bg-color: var(--jb-button-hover-background-color, #e0e0e0);
                        --accent-color: var(--jb-link-color, #0d6efd);
                        --user-message-bg: #e7f1ff;
                        --assistant-message-bg: var(--jb-content-background-color, #f2f2f2);

                        /* 暗色主题下的颜色覆盖 */
                        --user-message-bg-dark: #3b5980;
                        --assistant-message-bg-dark: #3c3f41;
                    }
                    
                    /* 适配 IntelliJ 暗色主题 */
                    html[class*="Theme--Darcula"] {
                         --user-message-bg: var(--user-message-bg-dark);
                         --assistant-message-bg: var(--assistant-message-bg-dark);
                    }

                    /* --- 基础样式 --- */
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        font-size: 14px;
                        margin: 0;
                        background-color: var(--bg-color);
                        color: var(--text-color);
                        overflow: hidden; /* 防止body滚动，让容器滚动 */
                    }

                    * {
                        box-sizing: border-box;
                    }

                    /* --- 布局容器 --- */
                    .chat-container {
                        display: flex;
                        flex-direction: column;
                        height: 100vh;
                        padding: 10px;
                    }

                    .chat-header {
                        font-size: 16px;
                        font-weight: bold;
                        padding-bottom: 10px;
                        border-bottom: 1px solid var(--border-color);
                        flex-shrink: 0; /* 防止头部被压缩 */
                        text-align: center;
                    }

                    /* --- 消息区域 --- */
                    .chat-messages {
                        flex-grow: 1; /* 占据所有可用空间 */
                        overflow-y: auto;
                        padding: 15px 5px;
                        scroll-behavior: smooth;
                    }
                    
                    .welcome-message {
                        text-align: center;
                        color: var(--jb-secondary-text-color, #888888);
                        margin-bottom: 20px;
                    }

                    /* --- 单条消息样式 --- */
                    .message {
                        display: flex;
                        margin-bottom: 12px;
                        max-width: 85%;
                    }
                    .message-content {
                        padding: 10px 14px;
                        border-radius: 18px;
                        line-height: 1.5;
                    }
                    .message-avatar {
                        width: 32px;
                        height: 32px;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-weight: bold;
                        flex-shrink: 0;
                    }

                    /* AI 助手消息 (靠左) */
                    .message.assistant {
                        align-self: flex-start;
                    }
                    .message.assistant .message-avatar {
                        background-color: var(--accent-color);
                        color: white;
                        margin-right: 10px;
                    }
                    .message.assistant .message-content {
                        background-color: var(--assistant-message-bg);
                        border-top-left-radius: 4px;
                    }

                    /* 用户消息 (靠右) */
                    .message.user {
                        align-self: flex-end;
                        flex-direction: row-reverse; /* 头像和内容反向 */
                    }
                    .message.user .message-avatar {
                        background-color: #7d7d7d;
                        color: white;
                        margin-left: 10px;
                    }
                    .message.user .message-content {
                        background-color: var(--user-message-bg);
                        color: var(--jb-text-color); /* 确保在暗色模式下文字可读 */
                        border-top-right-radius: 4px;
                    }

                    /* --- 输入区域 --- */
                    .chat-input {
                        padding-top: 10px;
                        border-top: 1px solid var(--border-color);
                        flex-shrink: 0; /* 防止输入区被压缩 */
                    }
                    .input-container {
                        display: flex;
                        align-items: center;
                    }
                    .message-input {
                        flex-grow: 1;
                        padding: 8px 12px;
                        border: 1px solid var(--border-color);
                        border-radius: 6px;
                        background-color: var(--input-bg-color);
                        color: var(--text-color);
                        margin-right: 10px;
                        outline: none;
                        transition: border-color 0.2s;
                    }
                    .message-input:focus {
                        border-color: var(--accent-color);
                    }
                    .message-input:disabled {
                        background-color: var(--jb-disabled-background-color, #f5f5f5);
                    }

                    .send-button {
                        padding: 8px 16px;
                        border: none;
                        border-radius: 6px;
                        background-color: var(--button-bg-color);
                        color: var(--button-text-color);
                        font-weight: 500;
                        cursor: pointer;
                        transition: background-color 0.2s;
                    }
                    .send-button:hover {
                        background-color: var(--button-hover-bg-color);
                    }
                    .send-button:disabled {
                        opacity: 0.6;
                        cursor: not-allowed;
                    }

                    /* --- 正在输入指示器 --- */
                    .typing-indicator {
                        display: none; /* 默认隐藏 */
                        padding: 10px 0 5px 15px;
                        color: var(--jb-secondary-text-color, #888888);
                        font-style: italic;
                    }

                    /* --- 美化滚动条 --- */
                    .chat-messages::-webkit-scrollbar {
                        width: 6px;
                    }
                    .chat-messages::-webkit-scrollbar-track {
                        background: transparent;
                    }
                    .chat-messages::-webkit-scrollbar-thumb {
                        background-color: var(--jb-scrollbar-thumb-color, #cccccc);
                        border-radius: 3px;
                    }
                    .chat-messages::-webkit-scrollbar-thumb:hover {
                        background-color: var(--jb-scrollbar-thumb-hover-color, #aaaaaa);
                    }
                </style>
            </head>
            <body>
                <div class="chat-container">
                    <div class="chat-header">🤖 AI 助手</div>
                    
                    <div class="chat-messages" id="chatMessages">
                        <div class="welcome-message">
                            你好！我是你的AI助手，有什么可以帮助你的吗？
                        </div>
                    </div>
                    
                    <div class="typing-indicator" id="typingIndicator">
                        AI 正在思考...
                    </div>
                    
                    <div class="chat-input">
                        <div class="input-container">
                            <input type="text" 
                                   class="message-input" 
                                   id="messageInput" 
                                   placeholder="输入你的消息..."
                                   onkeypress="handleKeyPress(event)">
                            <button class="send-button" id="sendButton" onclick="send()">发送</button>
                        </div>
                    </div>
                </div>
                
                <script>
                    const messageInput = document.getElementById('messageInput');
                    const sendButton = document.getElementById('sendButton');
                    const chatMessages = document.getElementById('chatMessages');
                    const typingIndicator = document.getElementById('typingIndicator');
                    
                    // 检查IDE主题并应用到html元素上，以便CSS可以适配
                    // JBCefBrowser 会自动为 <html> 标签添加 'Theme--Darcula' 或 'Theme--Light' class
                    // 但以防万一，我们也可以用 media query 作为备用
                    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
                        if (!document.documentElement.className.includes('Theme--Darcula')) {
                            // document.documentElement.classList.add('Theme--Darcula'); // JBCefBrowser通常会处理，此行可选
                        }
                    }

                    function handleKeyPress(event) {
                        if (event.key === 'Enter' && !sendButton.disabled) {
                            send();
                        }
                    }

                    function send() {
                        const message = messageInput.value.trim();
                        if (message === '') return;

                        addMessage(message, 'user');
                        messageInput.value = '';
                        
                        setInteractionDisabled(true);
                        showTypingIndicator();

                        if (window.sendMessage) {
                            window.sendMessage(
                                message,
                                function(response) { // onSuccess
                                    hideTypingIndicator();
                                    addMessage(response, 'assistant');
                                    setInteractionDisabled(false);
                                },
                                function(error) { // onFailure
                                    hideTypingIndicator();
                                    addMessage(`【错误】${'$'}{error}`, 'assistant');
                                    setInteractionDisabled(false);
                                }
                            );
                        } else {
                            // 备用逻辑，以防桥接未初始化
                            setTimeout(() => {
                                hideTypingIndicator();
                                addMessage('抱歉，与后端的连接似乎已断开。', 'assistant');
                                setInteractionDisabled(false);
                            }, 1000);
                        }
                    }

                    function addMessage(content, sender) {
                        const messageDiv = document.createElement('div');
                        messageDiv.className = `message ${'$'}{sender}`;

                        const avatar = document.createElement('div');
                        avatar.className = 'message-avatar';
                        avatar.textContent = sender === 'user' ? '你' : 'AI';

                        const messageContent = document.createElement('div');
                        messageContent.className = 'message-content';
                        // 为了安全和格式，纯文本内容使用 textContent
                        messageContent.textContent = content;

                        // 根据发送者决定头像和内容的顺序
                        if (sender === 'user') {
                            messageDiv.appendChild(messageContent);
                            messageDiv.appendChild(avatar);
                        } else {
                            messageDiv.appendChild(avatar);
                            messageDiv.appendChild(messageContent);
                        }

                        chatMessages.appendChild(messageDiv);
                        // 滚动到底部
                        chatMessages.scrollTop = chatMessages.scrollHeight;
                    }

                    function showTypingIndicator() {
                        typingIndicator.style.display = 'block';
                        chatMessages.scrollTop = chatMessages.scrollHeight;
                    }

                    function hideTypingIndicator() {
                        typingIndicator.style.display = 'none';
                    }
                    
                    function setInteractionDisabled(disabled) {
                        messageInput.disabled = disabled;
                        sendButton.disabled = disabled;
                        if (!disabled) {
                            messageInput.focus();
                        }
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        browser.loadHTML(htmlContent)
    }
}
