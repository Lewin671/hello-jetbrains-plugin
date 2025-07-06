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
            "/lint /Users/qingyingliu/IdeaProjects/hello-java/src/Test.kt",
            "/lint Test.kt",
            "/lint"
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
            "/usages String",
            "/usages ChatService",
            "/usages"
        )
        
        println("=== 快速测试 usages 命令 ===")
        for (testCase in testCases) {
            println("\n测试: '$testCase'")
            testUsagesCommand(testCase)
        }
    }
    
    /**
     * 测试其他斜杠命令
     */
    fun testSlashCommands() {
        val testCases = listOf(
            "/help",
            "/project",
            "/time",
            "/file",
            "/code",
            "/symbol"
        )
        
        println("=== 测试斜杠命令 ===")
        for (testCase in testCases) {
            println("\n测试: '$testCase'")
            val result = processMessage(testCase)
            println("输出: $result")
        }
    }
    
    /**
     * 综合测试所有斜杠命令
     */
    fun comprehensiveTest() {
        println("=== 综合测试所有斜杠命令 ===")
        
        // 测试斜杠命令
        println("\n1. 测试斜杠命令格式:")
        testSlashCommands()
        
        // 测试 lint 和 usages 命令
        println("\n2. 测试 lint 和 usages 命令:")
        quickTest()
        quickTestUsages()
        
        // 测试原有格式（应该不匹配）
        println("\n3. 测试原有格式（应该不匹配）:")
        val originalCommands = listOf(
            "help",
            "项目",
            "时间",
            "文件",
            "代码",
            "符号",
            "lint",
            "usages"
        )
        for (cmd in originalCommands) {
            println("\n测试: '$cmd'")
            val result = processMessage(cmd)
            println("输出: $result")
        }
    }
    
    /**
     * 测试绝对路径的 lint 命令
     */
    fun testAbsolutePathLint() {
        val testCases = listOf(
            "/lint /Users/qingyingliu/IdeaProjects/hello-java/src/AAA.java",
            "/lint /Users/qingyingliu/IdeaProjects/hello-java/src/Test.kt",
            "/lint /path/to/hello/file.txt",
            "/lint /Users/qingyingliu/Documents/hello-world.py"
        )
        
        println("=== 测试绝对路径的 lint 命令 ===")
        for (testCase in testCases) {
            println("\n测试: '$testCase'")
            val result = processMessage(testCase)
            println("输出: $result")
        }
    }
} 