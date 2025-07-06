package com.qingyingliu.hellojebrainsplugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ChatServiceTest : BasePlatformTestCase() {
    
    private lateinit var chatService: ChatService
    
    override fun setUp() {
        super.setUp()
        chatService = ChatService(project)
    }
    
    fun testGreetingCommand() {
        val response = chatService.processMessage("你好")
        assertTrue("问候命令应该返回包含问候语的响应", response.contains("你好！我是你的AI编程助手"))
    }
    
    fun testHelpCommand() {
        val response = chatService.processMessage("帮助")
        assertTrue("帮助命令应该返回帮助信息", response.contains("📋 可用命令"))
    }
    
    fun testTimeCommand() {
        val response = chatService.processMessage("时间")
        assertTrue("时间命令应该返回时间信息", response.contains("🕐 当前时间"))
    }
    
    fun testProjectCommand() {
        val response = chatService.processMessage("项目")
        assertTrue("项目命令应该返回项目信息", response.contains("📁 项目信息"))
    }
    
    fun testFileCommand() {
        val response = chatService.processMessage("文件")
        assertTrue("文件命令应该返回文件结构信息", response.contains("📂 项目文件结构"))
    }
    
    fun testCodeCommand() {
        val response = chatService.processMessage("代码")
        assertTrue("代码命令应该返回编程帮助", response.contains("💻 编程帮助"))
    }
    
    fun testSymbolCommand() {
        val response = chatService.processMessage("符号")
        assertTrue("符号命令应该返回符号信息", response.contains("🔖 当前打开文件中的符号"))
    }
    
    fun testLintCommand() {
        val response = chatService.processMessage("lint")
        assertTrue("Lint命令应该返回代码检查信息", response.contains("🔍 代码检查问题"))
    }
    
    fun testUsagesCommand() {
        val response = chatService.processMessage("usages")
        assertTrue("Usages命令应该返回帮助信息", response.contains("🔍 引用查找命令帮助"))
    }
    
    fun testDefaultResponse() {
        val response = chatService.processMessage("未知命令")
        assertTrue("未知命令应该返回默认响应", response.contains("我收到了你的消息"))
    }
    
    fun testEnglishCommands() {
        val greetingResponse = chatService.processMessage("hello")
        assertTrue("英文问候命令应该工作", greetingResponse.contains("你好！我是你的AI编程助手"))
        
        val helpResponse = chatService.processMessage("help")
        assertTrue("英文帮助命令应该工作", helpResponse.contains("📋 可用命令"))
        
        val timeResponse = chatService.processMessage("time")
        assertTrue("英文时间命令应该工作", timeResponse.contains("🕐 当前时间"))
        
        val projectResponse = chatService.processMessage("project")
        assertTrue("英文项目命令应该工作", projectResponse.contains("📁 项目信息"))
        
        val fileResponse = chatService.processMessage("file")
        assertTrue("英文文件命令应该工作", fileResponse.contains("📂 项目文件结构"))
        
        val codeResponse = chatService.processMessage("code")
        assertTrue("英文代码命令应该工作", codeResponse.contains("💻 编程帮助"))
        
        val symbolResponse = chatService.processMessage("symbol")
        assertTrue("英文符号命令应该工作", symbolResponse.contains("🔖 当前打开文件中的符号"))
    }
} 