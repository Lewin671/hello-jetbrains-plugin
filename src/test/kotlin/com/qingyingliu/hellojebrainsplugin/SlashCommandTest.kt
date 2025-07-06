package com.qingyingliu.hellojebrainsplugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.qingyingliu.hellojebrainsplugin.commands.*

/**
 * 斜杠命令测试
 */
class SlashCommandTest : BasePlatformTestCase() {
    
    fun testSlashLintCommand() {
        val handler = LintCommandHandler()
        
        // 测试斜杠命令格式
        assertTrue(handler.canHandle("/lint"))
        assertTrue(handler.canHandle("/lint Test.kt"))
        assertTrue(handler.canHandle("/lint /path/to/file.kt"))
        
        // 测试不匹配的情况
        assertFalse(handler.canHandle("lint"))
        assertFalse(handler.canHandle("检查"))
        assertFalse(handler.canHandle("问题"))
        assertFalse(handler.canHandle("hello"))
        assertFalse(handler.canHandle("random text"))
    }
    
    fun testSlashUsagesCommand() {
        val handler = UsagesCommandHandler()
        
        // 测试斜杠命令格式
        assertTrue(handler.canHandle("/usages"))
        assertTrue(handler.canHandle("/usages String"))
        assertTrue(handler.canHandle("/usages ChatService"))
        
        // 测试不匹配的情况
        assertFalse(handler.canHandle("usages"))
        assertFalse(handler.canHandle("引用"))
        assertFalse(handler.canHandle("用法"))
        assertFalse(handler.canHandle("hello"))
        assertFalse(handler.canHandle("random text"))
    }
    
    fun testSlashHelpCommand() {
        val handler = HelpCommandHandler()
        
        // 测试斜杠命令格式
        assertTrue(handler.canHandle("/help"))
        
        // 测试不匹配的情况
        assertFalse(handler.canHandle("help"))
        assertFalse(handler.canHandle("帮助"))
        assertFalse(handler.canHandle("hello"))
        assertFalse(handler.canHandle("random text"))
    }
    
    fun testAbsolutePathLintCommand() {
        val handler = LintCommandHandler()
        
        // 测试绝对路径的 lint 命令
        assertTrue(handler.canHandle("/lint /Users/qingyingliu/IdeaProjects/hello-java/src/AAA.java"))
        assertTrue(handler.canHandle("/lint /Users/qingyingliu/IdeaProjects/hello-java/src/Test.kt"))
        assertTrue(handler.canHandle("/lint /path/to/hello/file.txt"))
        assertTrue(handler.canHandle("/lint /Users/qingyingliu/Documents/hello-world.py"))
        
        // 测试相对路径
        assertTrue(handler.canHandle("/lint Test.kt"))
        assertTrue(handler.canHandle("/lint src/main/kotlin/MyFile.kt"))
    }
} 