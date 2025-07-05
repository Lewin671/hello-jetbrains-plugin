package com.qingyingliu.hellojebrainsplugin

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

class ChatPanel(private val project: Project) : JPanel(BorderLayout()) {
    
    private val chatService = ChatService(project)
    
    private val chatArea = JBTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        font = font.deriveFont(14f)
    }
    
    private val inputField = JBTextField().apply {
        preferredSize = Dimension(200, 30)
        font = font.deriveFont(14f)
    }
    
    private val sendButton = JButton("发送").apply {
        preferredSize = Dimension(60, 30)
    }
    
    init {
        setupUI()
        setupListeners()
        addWelcomeMessage()
    }
    
    private fun setupUI() {
        // 聊天区域
        val scrollPane = JBScrollPane(chatArea).apply {
            preferredSize = Dimension(300, 400)
            border = JBUI.Borders.customLine(JBUI.CurrentTheme.DefaultTabs.borderColor())
        }
        
        // 输入区域
        val inputPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
        }
        
        inputPanel.add(inputField, BorderLayout.CENTER)
        inputPanel.add(sendButton, BorderLayout.EAST)
        
        add(scrollPane, BorderLayout.CENTER)
        add(inputPanel, BorderLayout.SOUTH)
        
        // 设置整体边框
        border = JBUI.Borders.empty(5)
    }
    
    private fun setupListeners() {
        // 发送按钮点击事件
        sendButton.addActionListener {
            sendMessage()
        }
        
        // 回车键发送消息
        inputField.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    sendMessage()
                }
            }
        })
    }
    
    private fun sendMessage() {
        val message = inputField.text.trim()
        if (message.isNotEmpty()) {
            addMessage("你: $message")
            inputField.text = ""
            
            // 模拟回复
            processMessage(message)
        }
    }
    
    private fun processMessage(message: String) {
        // 使用聊天服务处理消息
        val response = chatService.processMessage(message)
        
        // 延迟显示回复，模拟思考时间
        Timer(500) { 
            addMessage("AI助手: $response")
            (it.source as Timer).stop()
        }.start()
    }
    
    private fun addMessage(message: String) {
        SwingUtilities.invokeLater {
            chatArea.append("$message\n\n")
            // 滚动到底部
            chatArea.caretPosition = chatArea.document.length
        }
    }
    
    private fun addWelcomeMessage() {
        addMessage("AI助手: 你好！我是你的AI助手，有什么可以帮助你的吗？")
    }
} 