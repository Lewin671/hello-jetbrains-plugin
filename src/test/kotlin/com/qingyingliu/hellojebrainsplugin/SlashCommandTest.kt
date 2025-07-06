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
    
    fun testSlashProjectCommand() {
        val handler = ProjectCommandHandler()
        
        // 测试斜杠命令格式
        assertTrue(handler.canHandle("/project"))
        
        // 测试不匹配的情况
        assertFalse(handler.canHandle("project"))
        assertFalse(handler.canHandle("项目"))
        assertFalse(handler.canHandle("hello"))
        assertFalse(handler.canHandle("random text"))
    }
    
    fun testSlashTimeCommand() {
        val handler = TimeCommandHandler()
        
        // 测试斜杠命令格式
        assertTrue(handler.canHandle("/time"))
        
        // 测试不匹配的情况
        assertFalse(handler.canHandle("time"))
        assertFalse(handler.canHandle("时间"))
        assertFalse(handler.canHandle("hello"))
        assertFalse(handler.canHandle("random text"))
    }
    
    fun testSlashFileCommand() {
        val handler = FileCommandHandler()
        
        // 测试斜杠命令格式
        assertTrue(handler.canHandle("/file"))
        
        // 测试不匹配的情况
        assertFalse(handler.canHandle("file"))
        assertFalse(handler.canHandle("文件"))
        assertFalse(handler.canHandle("hello"))
        assertFalse(handler.canHandle("random text"))
    }
    
    fun testSlashCodeCommand() {
        val handler = CodeCommandHandler()
        
        // 测试斜杠命令格式
        assertTrue(handler.canHandle("/code"))
        
        // 测试不匹配的情况
        assertFalse(handler.canHandle("code"))
        assertFalse(handler.canHandle("代码"))
        assertFalse(handler.canHandle("hello"))
        assertFalse(handler.canHandle("random text"))
    }
    
    fun testSlashSymbolCommand() {
        val handler = SymbolCommandHandler()
        
        // 测试斜杠命令格式
        assertTrue(handler.canHandle("/symbol"))
        
        // 测试不匹配的情况
        assertFalse(handler.canHandle("symbol"))
        assertFalse(handler.canHandle("符号"))
        assertFalse(handler.canHandle("hello"))
        assertFalse(handler.canHandle("random text"))
    }
} 