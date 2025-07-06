package com.qingyingliu.hellojebrainsplugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ChatServiceTest : BasePlatformTestCase() {
    
    private lateinit var chatService: ChatService
    
    override fun setUp() {
        super.setUp()
        chatService = ChatService(project)
    }
    
    fun testHelpCommand() {
        val response = chatService.processMessage("/help")
        assertTrue("帮助命令应该返回帮助信息", response.contains("📋 可用命令"))
    }
    
    fun testLintCommand() {
        val response = chatService.processMessage("/lint")
        println("=== Lint 命令返回内容 ===")
        println(response)
        println("==========================")
        assertTrue("Lint命令应该返回代码检查信息", response.contains("🔍 代码检查问题"))
    }
    
    fun testUsagesCommand() {
        val response = chatService.processMessage("/usages")
        println("=== Usages 命令返回内容 ===")
        println(response)
        println("===========================")
        assertTrue("Usages命令应该返回帮助信息", response.contains("🔍 引用查找命令帮助"))
    }
    
    fun testDefaultResponse() {
        val response = chatService.processMessage("未知命令")
        assertTrue("未知命令应该返回默认响应", response.contains("我收到了你的消息"))
    }
    
    fun testSlashCommands() {
        val helpResponse = chatService.processMessage("/help")
        assertTrue("斜杠帮助命令应该工作", helpResponse.contains("📋 可用命令"))
        
        val lintResponse = chatService.processMessage("/lint")
        assertTrue("斜杠lint命令应该工作", lintResponse.contains("🔍 代码检查问题"))
        
        val usagesResponse = chatService.processMessage("/usages")
        assertTrue("斜杠usages命令应该工作", usagesResponse.contains("🔍 引用查找命令帮助"))
    }
} 