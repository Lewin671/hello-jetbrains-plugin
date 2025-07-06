package com.qingyingliu.hellojebrainsplugin

import com.intellij.openapi.project.Project
import com.qingyingliu.hellojebrainsplugin.commands.CommandChain

/**
 * 聊天服务 - 重构后的版本
 * 
 * 使用设计模式重构：
 * 1. 策略模式 (Strategy Pattern) - 处理不同类型的命令
 * 2. 责任链模式 (Chain of Responsibility) - 处理命令匹配
 * 3. 单一职责原则 - 每个类只负责一个功能
 * 4. 开闭原则 - 易于扩展新命令
 */
class ChatService(private val project: Project) {
    
    // 使用单例模式管理命令链
    private val commandChain = CommandChain.createDefaultChain()
    
    /**
     * 处理用户消息
     * 
     * @param message 用户输入的消息
     * @return 处理后的响应
     */
    fun processMessage(message: String): String {
        return commandChain.process(message, project)
    }
    
    // ==================== 测试方法 ====================
    
    /**
     * 测试 lint 命令匹配
     */
    fun testLintCommand(message: String): String {
        println("=== 测试 lint 命令匹配 ===")
        println("输入消息: '$message'")
        
        val result = processMessage(message)
        
        println("=== 测试结果 ===")
        println("输出: $result")
        println("==================")
        
        return result
    }
    
    /**
     * 快速测试 lint 命令
     */
    fun quickTest() {
        val testCases = listOf(
            "lint /Users/qingyingliu/IdeaProjects/hello-java/src/Test.kt",
            "lint Test.kt",
            "检查 /path/to/file.kt",
            "问题 src/main/kotlin/MyFile.kt"
        )
        
        println("=== 快速测试 lint 命令 ===")
        for (testCase in testCases) {
            println("\n测试: '$testCase'")
            testLintCommand(testCase)
        }
    }
    
    /**
     * 测试 usages 命令匹配
     */
    fun testUsagesCommand(message: String): String {
        println("=== 测试 usages 命令匹配 ===")
        println("输入消息: '$message'")
        
        val result = processMessage(message)
        
        println("=== 测试结果 ===")
        println("输出: $result")
        println("==================")
        
        return result
    }
    
    /**
     * 快速测试 usages 命令
     */
    fun quickTestUsages() {
        val testCases = listOf(
            "usages String",
            "引用 ChatService",
            "用法 java.util.List",
            "usages processMessage",
            "引用 getUsagesForName",
            "用法 findClassByName",
            // 测试简单类名（无需包名）
            "usages ChatService",
            "引用 ChatPanel",
            "用法 ChatAction",
            "usages",
            "引用"
        )
        
        println("=== 快速测试 usages 命令 ===")
        for (testCase in testCases) {
            println("\n测试: '$testCase'")
            testUsagesCommand(testCase)
        }
    }
} 