package com.qingyingliu.hellojebrainsplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.actionSystem.ActionManager
import javax.swing.JFrame
import javax.swing.SwingUtilities
import java.awt.Dimension

/**
 * 聊天面板演示类
 * 
 * 这个类提供了一个独立的演示窗口来展示 JCEF 聊天面板的功能。
 * 可以通过菜单或快捷键调用来打开演示窗口。
 */
class ChatPanelDemo : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            showDemoWindow(project)
        } else {
            Messages.showErrorDialog("无法获取项目信息", "错误")
        }
    }
    
    private fun showDemoWindow(project: Project) {
        SwingUtilities.invokeLater {
            val frame = JFrame("AI助手 - JCEF 演示")
            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            frame.preferredSize = Dimension(500, 600)
            frame.minimumSize = Dimension(400, 500)
            
            // 创建聊天面板
            val chatPanel = ChatPanel(project)
            frame.contentPane.add(chatPanel)
            
            // 居中显示窗口
            frame.setLocationRelativeTo(null)
            frame.pack()
            frame.isVisible = true
            
            // 显示欢迎消息
            Messages.showInfoMessage(
                "JCEF 聊天面板演示已启动！\n\n" +
                "功能特点：\n" +
                "• 现代化的 Web 界面\n" +
                "• 支持斜杠命令（如 /help, /time, /project 等）\n" +
                "• 实时消息交互\n" +
                "• 响应式设计\n\n" +
                "试试输入一些命令吧！",
                "演示启动"
            )
        }
    }
    
    companion object {
        /**
         * 直接显示演示窗口（用于测试）
         */
        @JvmStatic
        fun showDemo(project: Project) {
            val demo = ChatPanelDemo()
            // 构造一个简单的 DataContext
            val dataContext: DataContext = SimpleDataContext.getProjectContext(project)
            val event = AnActionEvent(
                null,
                dataContext,
                "ChatPanelDemo",
                demo.templatePresentation,
                ActionManager.getInstance(),
                0
            )
            demo.actionPerformed(event)
        }
    }
} 