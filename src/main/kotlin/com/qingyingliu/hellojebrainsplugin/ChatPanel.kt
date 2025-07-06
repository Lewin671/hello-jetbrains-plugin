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
        browser.loadURL("http://192.168.7.136:3000")
    }
}
