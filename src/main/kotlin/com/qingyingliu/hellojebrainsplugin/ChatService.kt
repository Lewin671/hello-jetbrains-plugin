package com.qingyingliu.hellojebrainsplugin

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.multiverse.codeInsightContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import com.intellij.psi.PsiErrorElement
import com.intellij.util.Processors
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File


class ChatService(private val project: Project) {

    fun processMessage(message: String): String {
        return when {
            message.contains("你好") || message.contains("hello") -> getGreeting()
            message.contains("帮助") || message.contains("help") -> getHelpMessage()
            message.contains("时间") || message.contains("time") -> getCurrentTime()
            message.contains("项目") || message.contains("project") -> getProjectInfo()
            message.contains("文件") || message.contains("file") -> getFileInfo()
            message.contains("代码") || message.contains("code") -> getCodeHelp()
            message.contains("符号") || message.contains("symbol") -> getOpenSymbols()
            message.contains("lint") || message.contains("检查") || message.contains("问题") -> {
                // 检查是否包含路径信息
                val pathMatch = Regex("lint\\s+(.+)|检查\\s+(.+)|问题\\s+(.+)").find(message)
                if (pathMatch != null) {
                    val path = pathMatch.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
                    if (path != null) {
                        getLintInfoForPath(path.trim())
                    } else {
                        getLintInfo()
                    }
                } else {
                    getLintInfo()
                }
            }
            else -> getDefaultResponse(message)
        }
    }

    private fun getGreeting(): String {
        return "你好！我是你的AI编程助手 🤖\n\n" +
                "我可以帮助你：\n" +
                "• 分析代码和项目结构\n" +
                "• 提供编程建议\n" +
                "• 回答技术问题\n" +
                "• 查看项目信息\n\n" +
                "试试输入 '帮助' 查看更多功能！"
    }

    private fun getHelpMessage(): String {
        return "📋 可用命令：\n\n" +
                "🔍 项目相关：\n" +
                "• '项目' - 查看当前项目信息\n" +
                "• '文件' - 查看项目文件结构\n\n" +
                "💻 编程相关：\n" +
                "• '代码' - 获取编程帮助\n" +
                "• '符号' - 查看当前打开文件的符号\n" +
                "• 'lint' 或 '检查' - 查看当前文件的代码检查问题\n" +
                "• 'lint 文件路径' - 查看指定文件的代码检查问题\n" +
                "• 直接询问编程问题\n\n" +
                "⏰ 其他功能：\n" +
                "• '时间' - 显示当前时间\n" +
                "• '帮助' - 显示此帮助信息\n\n" +
                "💡 提示：你可以用中文或英文与我交流！"
    }

    private fun getCurrentTime(): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return "🕐 当前时间：${now.format(formatter)}"
    }

    private fun getProjectInfo(): String {
        val projectName = project.name
        val projectPath = project.basePath ?: "未知路径"
        val fileCount = getProjectFileCount()

        return "📁 项目信息：\n\n" +
                "项目名称：$projectName\n" +
                "项目路径：$projectPath\n" +
                "文件数量：$fileCount 个文件\n\n" +
                "这是一个 JetBrains IDE 插件项目，使用 Kotlin 开发。"
    }

    private fun getFileInfo(): String {
        val projectPath = project.basePath ?: return "无法获取项目路径"
        val rootDir = project.baseDir

        return "📂 项目文件结构：\n\n" +
                "根目录：${rootDir.name}\n" +
                "主要文件类型：\n" +
                "• Kotlin 源文件 (.kt)\n" +
                "• 配置文件 (build.gradle.kts, plugin.xml)\n" +
                "• 资源文件\n\n" +
                "这是一个标准的 IntelliJ Platform 插件项目结构。"
    }

    private fun getCodeHelp(): String {
        return "💻 编程帮助：\n\n" +
                "我可以帮助你：\n" +
                "• 分析代码逻辑\n" +
                "• 提供最佳实践建议\n" +
                "• 解释编程概念\n" +
                "• 调试代码问题\n\n" +
                "请直接描述你的编程问题，我会尽力帮助你！\n\n" +
                "例如：\n" +
                "• '如何创建一个新的工具窗口？'\n" +
                "• 'Kotlin 中的扩展函数是什么？'\n" +
                "• '如何注册一个动作？'"
    }

    private fun getOpenSymbols(): String {
        val editorManager = FileEditorManager.getInstance(project)
        val openFiles = editorManager.selectedFiles

        if (openFiles.isEmpty()) {
            return "当前没有打开任何文件。"
        }

        val psiManager = PsiManager.getInstance(project)
        val builder = StringBuilder("🔖 当前打开文件中的符号：\n\n")

        for (virtualFile in openFiles) {
            val psiFile = psiManager.findFile(virtualFile) ?: continue

            val symbols = mutableListOf<SymbolInfo>()

//            psiFile.accept(object : PsiRecursiveElementWalkingVisitor() {
//                override fun visitElement(element: PsiElement) {
//                    val symbolInfo = getSymbolInfo(element)
//                    if (symbolInfo != null) {
//                        symbols.add(symbolInfo)
//                    }
//                    super.visitElement(element)
//                }
//            })

            builder.append("文件: ").append(virtualFile.name).append("\n")

            if (symbols.isEmpty()) {
                builder.append("  (无符号)\n\n")
            } else {
                // 按类型分组
                val groupedSymbols = symbols.groupBy { it.type }

                // 按类型顺序显示
                val typeOrder = listOf("类", "接口", "对象", "函数", "属性", "参数", "类型别名", "变量", "其他")

                for (type in typeOrder) {
                    val typeSymbols = groupedSymbols[type] ?: continue
                    if (typeSymbols.isNotEmpty()) {
                        builder.append("  📌 $type:\n")
                        typeSymbols.distinctBy { it.fullQualifiedName }.forEach { symbol ->
                            builder.append("    • ${symbol.fullQualifiedName}\n")
                        }
                        builder.append("\n")
                    }
                }
            }
        }

        return builder.toString()
    }

    private data class SymbolInfo(
        val name: String,
        val type: String,
        val fullQualifiedName: String
    )

    private fun getLintInfo(): String {
        val editorManager = FileEditorManager.getInstance(project)
        val openFiles = editorManager.selectedFiles

        if (openFiles.isEmpty()) {
            return "当前没有打开任何文件。请先打开一个文件，然后使用 'lint' 命令查看代码检查问题。\n\n" +
                   "💡 提示：你也可以指定文件路径，例如：\n" +
                   "• 'lint /path/to/file.kt'\n" +
                   "• '检查 src/main/kotlin/MyFile.kt'\n" +
                   "• '问题 ChatService.kt'"
        }

        val builder = StringBuilder("🔍 代码检查问题：\n\n")

        for (virtualFile in openFiles) {
            val result = analyzeFileForLint(virtualFile)
            builder.append(result)
        }

        return builder.toString()
    }

    private fun getLintInfoForPath(filePath: String): String {
        val builder = StringBuilder("🔍 代码检查问题：\n\n")
        
        // 处理相对路径和绝对路径
        val resolvedPath = when {
            filePath.startsWith("/") -> filePath // 绝对路径
            filePath.startsWith("~") -> filePath.replaceFirst("~", System.getProperty("user.home")) // 用户主目录
            else -> {
                // 相对路径，尝试从项目根目录解析
                val projectPath = project.basePath
                if (projectPath != null) {
                    "$projectPath/$filePath"
                } else {
                    filePath
                }
            }
        }

        val file = File(resolvedPath)
        if (!file.exists()) {
            return "❌ 文件不存在：$resolvedPath\n\n" +
                   "💡 提示：请检查文件路径是否正确，或者使用绝对路径。"
        }

        if (!file.isFile) {
            return "❌ 指定路径不是文件：$resolvedPath\n\n" +
                   "💡 提示：请指定一个具体的文件路径，而不是目录。"
        }

        // 获取 VirtualFile
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file)
        if (virtualFile == null) {
            return "❌ 无法访问文件：$resolvedPath\n\n" +
                   "💡 提示：请确保文件存在且有读取权限。"
        }

        val result = analyzeFileForLint(virtualFile)
        builder.append(result)

        return builder.toString()
    }

    private fun analyzeFileForLint(virtualFile: VirtualFile): String {
        val builder = StringBuilder()
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
        
        if (psiFile == null) {
            builder.append("📄 文件: ${virtualFile.name}\n")
            builder.append("  ❌ 无法解析文件内容\n\n")
            return builder.toString()
        }

        builder.append("📄 文件: ${virtualFile.name}\n")
        builder.append("📍 路径: ${virtualFile.path}\n")

        // 获取编辑器实例
        val editorManager = FileEditorManager.getInstance(project)
        val editor = editorManager.getSelectedTextEditor()
        
        if (editor == null) {
            // 如果文件没有在编辑器中打开，我们需要创建一个临时的编辑器
            val document = psiFile.viewProvider.document
            if (document == null) {
                builder.append("  ❌ 无法获取文档内容\n\n")
                return builder.toString()
            }
            
            return analyzeDocumentForLint(document, virtualFile.name, virtualFile.path)
        }

        return analyzeDocumentForLint(editor.document, virtualFile.name, virtualFile.path)
    }

    private fun analyzeDocumentForLint(document: Document, fileName: String, filePath: String): String {
        val builder = StringBuilder()
        builder.append("📄 文件: $fileName\n")
        builder.append("📍 路径: $filePath\n")

        val highlights = mutableListOf<HighlightInfo>()

        // 获取错误级别的问题
        DaemonCodeAnalyzerEx.processHighlights(
            document,
            project,
            HighlightSeverity.ERROR,
            document.getLineStartOffset(0),
            document.getLineEndOffset(document.lineCount - 1),
            Processors.cancelableCollectProcessor(highlights)
        )

        // 获取警告级别的问题
        val warnings = mutableListOf<HighlightInfo>()
        DaemonCodeAnalyzerEx.processHighlights(
            document,
            project,
            HighlightSeverity.WARNING,
            document.getLineStartOffset(0),
            document.getLineEndOffset(document.lineCount - 1),
            Processors.cancelableCollectProcessor(warnings)
        )

        // 获取弱警告级别的问题
        val weakWarnings = mutableListOf<HighlightInfo>()
        DaemonCodeAnalyzerEx.processHighlights(
            document,
            project,
            HighlightSeverity.WEAK_WARNING,
            document.getLineStartOffset(0),
            document.getLineEndOffset(document.lineCount - 1),
            Processors.cancelableCollectProcessor(weakWarnings)
        )

        val totalProblems = highlights.size + warnings.size + weakWarnings.size

        if (totalProblems == 0) {
            builder.append("  ✅ 没有发现代码问题\n\n")
        } else {
            builder.append("  📊 总计发现 $totalProblems 个问题：\n")
            builder.append("    • 错误: ${highlights.size} 个\n")
            builder.append("    • 警告: ${warnings.size} 个\n")
            builder.append("    • 弱警告: ${weakWarnings.size} 个\n\n")

            // 显示错误详情
            if (highlights.isNotEmpty()) {
                builder.append("  ❌ 错误详情：\n")
                highlights.forEachIndexed { index, highlight ->
                    val line = document.getLineNumber(highlight.startOffset) + 1
                    val column = highlight.startOffset - document.getLineStartOffset(line - 1) + 1
                    val description = highlight.description ?: "未知错误"
                    val tooltip = highlight.toolTip ?: description
                    
                    builder.append("    ${index + 1}. 第 ${line} 行，第 ${column} 列\n")
                    builder.append("       描述: $description\n")
                    if (tooltip != description) {
                        builder.append("       详情: $tooltip\n")
                    }
                    builder.append("\n")
                }
            }

        }

        return builder.toString()
    }

    private data class ProblemInfo(
        val type: String,
        val line: Int,
        val column: Int,
        val text: String,
        val description: String
    )

    private fun getDefaultResponse(message: String): String {
        return "我收到了你的消息：\"$message\"\n\n" +
                "虽然我目前还不能完全理解你的具体需求，但我可以：\n" +
                "• 回答编程相关问题\n" +
                "• 提供项目信息\n" +
                "• 解释技术概念\n\n" +
                "请尝试更具体的问题，或者输入 '帮助' 查看可用功能！"
    }

    private fun getProjectFileCount(): Int {
        return try {
            project.baseDir.children.size
        } catch (e: Exception) {
            0
        }
    }
} 