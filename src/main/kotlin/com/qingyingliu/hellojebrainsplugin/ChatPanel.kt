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
        // 处理从JavaScript发送的消息
        jsQuery.addHandler { message ->
            try {
                val response = chatService.processMessage(message)
                sendResponseToJavaScript(response)
                null
            } catch (e: Exception) {
                e.printStackTrace()
                sendResponseToJavaScript("抱歉，处理消息时出现错误: ${e.message}")
                null
            }
        }
        
        // 注入jsQuery到window（可选，实际通信用jsQuery.invoke）
        browser.cefBrowser.executeJavaScript(
            "window.Java = { jsQuery: {} };", browser.cefBrowser.url, 0
        )
    }
    
    private fun loadChatInterface() {
        val htmlContent = """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>AI助手</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background-color: #f5f5f5;
                        height: 100vh;
                        display: flex;
                        flex-direction: column;
                    }
                    
                    .chat-container {
                        flex: 1;
                        display: flex;
                        flex-direction: column;
                        background: white;
                        border-radius: 8px;
                        margin: 8px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    
                    .chat-header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 16px;
                        text-align: center;
                        font-weight: 600;
                        font-size: 16px;
                    }
                    
                    .chat-messages {
                        flex: 1;
                        padding: 16px;
                        overflow-y: auto;
                        max-height: 400px;
                    }
                    
                    .message {
                        margin-bottom: 16px;
                        display: flex;
                        align-items: flex-start;
                    }
                    
                    .message.user {
                        justify-content: flex-end;
                    }
                    
                    .message.assistant {
                        justify-content: flex-start;
                    }
                    
                    .message-content {
                        max-width: 70%;
                        padding: 12px 16px;
                        border-radius: 18px;
                        word-wrap: break-word;
                        line-height: 1.4;
                    }
                    
                    .message.user .message-content {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        border-bottom-right-radius: 4px;
                    }
                    
                    .message.assistant .message-content {
                        background: #f1f3f4;
                        color: #333;
                        border-bottom-left-radius: 4px;
                    }
                    
                    .message-avatar {
                        width: 32px;
                        height: 32px;
                        border-radius: 50%;
                        margin: 0 8px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-weight: bold;
                        font-size: 14px;
                    }
                    
                    .message.user .message-avatar {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                    }
                    
                    .message.assistant .message-avatar {
                        background: #e8eaed;
                        color: #5f6368;
                    }
                    
                    .chat-input {
                        padding: 16px;
                        border-top: 1px solid #e0e0e0;
                        background: white;
                    }
                    
                    .input-container {
                        display: flex;
                        gap: 8px;
                    }
                    
                    .message-input {
                        flex: 1;
                        padding: 12px 16px;
                        border: 2px solid #e0e0e0;
                        border-radius: 24px;
                        font-size: 14px;
                        outline: none;
                        transition: border-color 0.3s;
                    }
                    
                    .message-input:focus {
                        border-color: #667eea;
                    }
                    
                    .send-button {
                        padding: 12px 20px;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        border: none;
                        border-radius: 24px;
                        cursor: pointer;
                        font-weight: 600;
                        transition: transform 0.2s;
                    }
                    
                    .send-button:hover {
                        transform: translateY(-1px);
                    }
                    
                    .send-button:active {
                        transform: translateY(0);
                    }
                    
                    .typing-indicator {
                        display: none;
                        padding: 12px 16px;
                        background: #f1f3f4;
                        border-radius: 18px;
                        border-bottom-left-radius: 4px;
                        color: #666;
                        font-style: italic;
                        margin-bottom: 16px;
                    }
                    
                    .welcome-message {
                        text-align: center;
                        color: #666;
                        font-style: italic;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="chat-container">
                    <div class="chat-header">
                        🤖 AI助手
                    </div>
                    
                    <div class="chat-messages" id="chatMessages">
                        <div class="welcome-message">
                            你好！我是你的AI助手，有什么可以帮助你的吗？
                        </div>
                    </div>
                    
                    <div class="typing-indicator" id="typingIndicator">
                        AI助手正在思考...
                    </div>
                    
                    <div class="chat-input">
                        <div class="input-container">
                            <input type="text" 
                                   class="message-input" 
                                   id="messageInput" 
                                   placeholder="输入你的消息..."
                                   onkeypress="handleKeyPress(event)">
                            <button class="send-button" onclick="sendMessage()">发送</button>
                        </div>
                    </div>
                </div>
                
                <script>
                    let jsQuery = null;
                    
                    // 初始化JavaScript查询对象
                    function initJSQuery() {
                        if (window.Java) {
                            jsQuery = window.Java.jsQuery;
                        }
                    }
                    
                    // 处理回车键
                    function handleKeyPress(event) {
                        if (event.key === 'Enter') {
                            sendMessage();
                        }
                    }
                    
                    // 发送消息
                    function sendMessage() {
                        const input = document.getElementById('messageInput');
                        const message = input.value.trim();
                        
                        if (message === '') return;
                        
                        // 添加用户消息
                        addMessage(message, 'user');
                        input.value = '';
                        
                        // 显示输入指示器
                        showTypingIndicator();
                        
                        // 发送到Java后端
                        if (jsQuery) {
                            jsQuery.invoke(message, function(response) {
                                hideTypingIndicator();
                                addMessage(response, 'assistant');
                            });
                        } else {
                            // 模拟响应（如果JavaScript桥接不可用）
                            setTimeout(() => {
                                hideTypingIndicator();
                                addMessage('抱歉，JavaScript桥接不可用', 'assistant');
                            }, 1000);
                        }
                    }
                    
                    // 添加消息到聊天区域
                    function addMessage(content, sender) {
                        const chatMessages = document.getElementById('chatMessages');
                        const messageDiv = document.createElement('div');
                        messageDiv.className = `message ${'$'}{sender}`;
                        
                        const avatar = document.createElement('div');
                        avatar.className = 'message-avatar';
                        avatar.textContent = sender === 'user' ? '你' : 'AI';
                        
                        const messageContent = document.createElement('div');
                        messageContent.className = 'message-content';
                        messageContent.textContent = content;
                        
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
                    
                    // 显示输入指示器
                    function showTypingIndicator() {
                        const indicator = document.getElementById('typingIndicator');
                        indicator.style.display = 'block';
                        document.getElementById('chatMessages').scrollTop = 
                            document.getElementById('chatMessages').scrollHeight;
                    }
                    
                    // 隐藏输入指示器
                    function hideTypingIndicator() {
                        const indicator = document.getElementById('typingIndicator');
                        indicator.style.display = 'none';
                    }
                    
                    // 页面加载完成后初始化
                    document.addEventListener('DOMContentLoaded', function() {
                        initJSQuery();
                    });
                </script>
            </body>
            </html>
        """.trimIndent()
        
        browser.loadHTML(htmlContent)
    }
    
    private fun sendResponseToJavaScript(response: String) {
        browser.cefBrowser.executeJavaScript(
            "if (typeof addMessage === 'function') { addMessage('${response.replace("'", "\\'")}', 'assistant'); }",
            browser.cefBrowser.url, 0
        )
    }
} 