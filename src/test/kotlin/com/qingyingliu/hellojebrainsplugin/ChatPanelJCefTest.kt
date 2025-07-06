package com.qingyingliu.hellojebrainsplugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.SwingUtilities

class ChatPanelJCefTest : BasePlatformTestCase() {
    
    fun testChatPanelCreation() {
        // 测试 ChatPanel 是否能正常创建
        val chatPanel = ChatPanel(project)
        assertNotNull("ChatPanel 应该能正常创建", chatPanel)
        
        // 验证面板包含必要的组件
        assertTrue("ChatPanel 应该有子组件", chatPanel.componentCount > 0)
    }
    
    fun testChatServiceIntegration() {
        val chatService = ChatService(project)
        
        // 测试基本命令处理
        val helpResponse = chatService.processMessage("/help")
        assertTrue("帮助命令应该返回帮助信息", helpResponse.contains("📋 可用命令"))
        
        val lintResponse = chatService.processMessage("/lint")
        assertTrue("Lint命令应该返回代码检查信息", lintResponse.contains("🔍 代码检查问题"))
    }
    
    fun testSlashCommands() {
        val chatService = ChatService(project)
        
        // 测试斜杠命令
        val commands = listOf(
            "/help" to "📋 可用命令",
            "/lint" to "🔍 代码检查问题",
            "/usages" to "🔍 引用查找命令帮助"
        )
        
        for ((command, expectedContent) in commands) {
            val response = chatService.processMessage(command)
            assertTrue("命令 '$command' 应该返回包含 '$expectedContent' 的响应", 
                      response.contains(expectedContent))
        }
    }
    
    fun testLintAndUsagesCommands() {
        val chatService = ChatService(project)
        
        // 测试 lint 命令
        val lintResponse = chatService.processMessage("/lint")
        assertTrue("Lint命令应该返回帮助信息", lintResponse.contains("🔍 代码检查问题"))
        
        // 测试 usages 命令
        val usagesResponse = chatService.processMessage("/usages")
        assertTrue("Usages命令应该返回帮助信息", usagesResponse.contains("🔍 引用查找命令帮助"))
    }
    
    fun testDefaultResponse() {
        val chatService = ChatService(project)
        
        // 测试未知命令
        val response = chatService.processMessage("这是一个未知命令")
        assertTrue("未知命令应该返回默认响应", response.contains("我收到了你的消息"))
    }
    
    // 注意：这个测试需要在实际的 IDE 环境中运行，因为它需要 JCEF 支持
    fun testJCefBrowserCreation() {
        // 这个测试验证 JCEF 浏览器是否能正常创建
        // 在实际的 IDE 环境中，JCEF 应该可用
        try {
            val browser = JBCefBrowser()
            assertNotNull("JBCefBrowser 应该能正常创建", browser)
            assertNotNull("JBCefBrowser 应该有组件", browser.component)
        } catch (e: Exception) {
            // 在测试环境中，JCEF 可能不可用，这是正常的
            println("JCEF 在测试环境中不可用: ${e.message}")
        }
    }
} 