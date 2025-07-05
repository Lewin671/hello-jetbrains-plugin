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
// 新增导入语句用于引用搜索功能
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiField
import com.intellij.psi.PsiVariable
import com.intellij.psi.PsiPackage
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiClassOwner
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiNameIdentifierOwner


class ChatService(private val project: Project) {

    fun processMessage(message: String): String {
        // 清理消息，去除不可见字符和换行符
        val cleanMessage = message.trim().replace(Regex("[\\r\\n\\t]+"), " ")
        println("DEBUG: 原始消息: '$message'")
        println("DEBUG: 清理后消息: '$cleanMessage'")
        println("DEBUG: 消息长度: ${cleanMessage.length}")
        println("DEBUG: 包含 'lint': ${cleanMessage.contains("lint")}")
        println("DEBUG: 包含 '检查': ${cleanMessage.contains("检查")}")
        println("DEBUG: 包含 '问题': ${cleanMessage.contains("问题")}")
        println("DEBUG: 消息转小写: '${cleanMessage.lowercase()}'")
        println("DEBUG: 转小写后包含 'lint': ${cleanMessage.lowercase().contains("lint")}")
        println("DEBUG: 消息是否以 'lint' 开头: ${cleanMessage.startsWith("lint")}")
        println("DEBUG: 消息是否以 'lint' 开头（忽略大小写）: ${cleanMessage.lowercase().startsWith("lint")}")
        
        // 检查其他可能的匹配
        println("DEBUG: 包含 'java': ${cleanMessage.contains("java")}")
        println("DEBUG: 包含 'project': ${cleanMessage.contains("project")}")
        println("DEBUG: 包含 'file': ${cleanMessage.contains("file")}")
        println("DEBUG: 包含 'code': ${cleanMessage.contains("code")}")
        println("DEBUG: 包含 'usages': ${cleanMessage.contains("usages")}")
        println("DEBUG: 包含 '引用': ${cleanMessage.contains("引用")}")
        
        return when {
            cleanMessage.contains("lint") || cleanMessage.lowercase().contains("lint") || cleanMessage.contains("检查") || cleanMessage.contains("问题") -> {
                println("DEBUG: 匹配到 lint 相关命令")
                // 检查是否包含路径信息
                val pathMatch = Regex("lint\\s+(.+)|检查\\s+(.+)|问题\\s+(.+)").find(cleanMessage)
                println("DEBUG: 路径匹配结果: $pathMatch")
                if (pathMatch != null) {
                    val path = pathMatch.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
                    println("DEBUG: 提取的路径: '$path'")
                    if (path != null) {
                        getLintInfoForPath(path.trim())
                    } else {
                        getLintInfo()
                    }
                } else {
                    // 如果没有匹配到路径，尝试更宽松的匹配
                    println("DEBUG: 尝试更宽松的路径匹配")
                    val looseMatch = Regex("lint\\s*(.+)|检查\\s*(.+)|问题\\s*(.+)").find(cleanMessage)
                    println("DEBUG: 宽松匹配结果: $looseMatch")
                    if (looseMatch != null) {
                        val path = looseMatch.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
                        println("DEBUG: 宽松匹配提取的路径: '$path'")
                        if (path != null) {
                            getLintInfoForPath(path.trim())
                        } else {
                            getLintInfo()
                        }
                    } else {
                        getLintInfo()
                    }
                }
            }
            cleanMessage.contains("你好") || cleanMessage.contains("hello") -> {
                println("DEBUG: 匹配到 '你好' 或 'hello'")
                getGreeting()
            }
            cleanMessage.contains("帮助") || cleanMessage.contains("help") -> {
                println("DEBUG: 匹配到 '帮助' 或 'help'")
                getHelpMessage()
            }
            cleanMessage.contains("时间") || cleanMessage.contains("time") -> {
                println("DEBUG: 匹配到 '时间' 或 'time'")
                getCurrentTime()
            }
            cleanMessage.contains("项目") || cleanMessage.contains("project") -> {
                println("DEBUG: 匹配到 '项目' 或 'project'")
                getProjectInfo()
            }
            cleanMessage.contains("文件") || cleanMessage.contains("file") -> {
                println("DEBUG: 匹配到 '文件' 或 'file'")
                getFileInfo()
            }
            cleanMessage.contains("代码") || cleanMessage.contains("code") -> {
                println("DEBUG: 匹配到 '代码' 或 'code'")
                getCodeHelp()
            }
            cleanMessage.contains("符号") || cleanMessage.contains("symbol") -> {
                println("DEBUG: 匹配到 '符号' 或 'symbol'")
                getOpenSymbols()
            }
            cleanMessage.contains("usages") || cleanMessage.contains("引用") || cleanMessage.contains("用法") -> {
                println("DEBUG: 匹配到 usages 相关命令")
                // 检查是否包含类名参数
                val classNameMatch = Regex("usages\\s+(.+)|引用\\s+(.+)|用法\\s+(.+)").find(cleanMessage)
                println("DEBUG: 类名匹配结果: $classNameMatch")
                if (classNameMatch != null) {
                    val className = classNameMatch.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
                    println("DEBUG: 提取的类名: '$className'")
                    if (className != null) {
                        getUsagesForClassName(className.trim())
                    } else {
                        getUsagesHelp()
                    }
                } else {
                    // 如果没有匹配到类名，尝试更宽松的匹配
                    println("DEBUG: 尝试更宽松的类名匹配")
                    val looseMatch = Regex("usages\\s*(.+)|引用\\s*(.+)|用法\\s*(.+)").find(cleanMessage)
                    println("DEBUG: 宽松匹配结果: $looseMatch")
                    if (looseMatch != null) {
                        val className = looseMatch.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
                        println("DEBUG: 宽松匹配提取的类名: '$className'")
                        if (className != null) {
                            getUsagesForClassName(className.trim())
                        } else {
                            getUsagesHelp()
                        }
                    } else {
                        getUsagesHelp()
                    }
                }
            }
            else -> {
                println("DEBUG: 没有匹配到任何条件，使用默认响应")
                getDefaultResponse(message)
            }
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
                "• 'usages 类名' 或 '引用 类名' - 查找指定类的所有引用\n" +
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

    // 测试方法，用于验证 lint 命令匹配
    fun testLintCommand(message: String): String {
        println("=== 测试 lint 命令匹配 ===")
        println("输入消息: '$message'")
        
        val result = processMessage(message)
        
        println("=== 测试结果 ===")
        println("输出: $result")
        println("==================")
        
        return result
    }
    
    // 简单的测试方法，用于快速验证
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

    // ==================== get_usages 相关方法 ====================

    /**
     * 获取指定类名的所有引用
     */
    private fun getUsagesForClassName(className: String): String {
        println("DEBUG: 开始查找类 '$className' 的引用")
        
        val builder = StringBuilder("🔍 查找类 '$className' 的引用：\n\n")
        
        try {
            // 1. 首先尝试查找类定义
            val psiClass = findClassByName(className)
            
            if (psiClass == null) {
                builder.append("❌ 未找到类 '$className'\n\n")
                builder.append("💡 提示：\n")
                builder.append("• 请检查类名是否正确（区分大小写）\n")
                builder.append("• 如果是内部类，请使用 'OuterClass.InnerClass' 格式\n")
                builder.append("• 如果是全限定类名，请包含包名\n")
                builder.append("• 例如：'java.lang.String' 或 'MyClass'\n\n")
                return builder.toString()
            }
            
            // 2. 显示类的基本信息
            builder.append("📋 类信息：\n")
            builder.append("• 类名: ${psiClass.name}\n")
            builder.append("• 全限定名: ${psiClass.qualifiedName}\n")
            builder.append("• 包名: ${psiClass.qualifiedName?.substringBeforeLast('.') ?: "未知"}\n")
            builder.append("• 文件: ${psiClass.containingFile?.name ?: "未知"}\n")
            builder.append("• 位置: ${psiClass.containingFile?.virtualFile?.path ?: "未知"}\n\n")
            
            // 3. 查找所有引用
            val usages = findUsages(psiClass)
            
            if (usages.isEmpty()) {
                builder.append("✅ 没有找到任何引用\n\n")
                builder.append("💡 说明：\n")
                builder.append("• 这个类可能没有被使用\n")
                builder.append("• 或者引用在注释或字符串中（不会被索引）\n")
                builder.append("• 或者引用在项目范围之外\n\n")
            } else {
                builder.append("📊 找到 ${usages.size} 个引用：\n\n")
                
                // 按文件分组显示引用
                val usagesByFile = usages.groupBy { it.containingFile }
                
                usagesByFile.forEach { (file, fileUsages) ->
                    builder.append("📄 文件: ${file?.name ?: "未知文件"}\n")
                    builder.append("📍 路径: ${file?.virtualFile?.path ?: "未知路径"}\n")
                    
                    fileUsages.forEachIndexed { index, element ->
                        val line = getElementLineNumber(element)
                        val column = getElementColumnNumber(element)
                        val context = getElementContext(element)
                        
                        builder.append("  ${index + 1}. 第 ${line} 行，第 ${column} 列\n")
                        builder.append("     上下文: $context\n")
                        builder.append("     内容: ${element.text.take(100)}${if (element.text.length > 100) "..." else ""}\n\n")
                    }
                }
            }
            
        } catch (e: Exception) {
            builder.append("❌ 查找过程中发生错误：${e.message}\n\n")
            builder.append("💡 可能的原因：\n")
            builder.append("• 项目索引可能不完整\n")
            builder.append("• 类名格式不正确\n")
            builder.append("• 项目配置问题\n\n")
            e.printStackTrace()
        }
        
        return builder.toString()
    }

    /**
     * 显示 usages 命令的帮助信息
     */
    private fun getUsagesHelp(): String {
        return "🔍 引用查找命令帮助：\n\n" +
                "📝 用法：\n" +
                "• 'usages 类名' - 查找指定类的所有引用\n" +
                "• '引用 类名' - 查找指定类的所有引用\n" +
                "• '用法 类名' - 查找指定类的所有引用\n\n" +
                "💡 示例：\n" +
                "• 'usages String' - 查找 java.lang.String 的引用\n" +
                "• '引用 ChatService' - 查找 ChatService 类的引用\n" +
                "• '用法 java.util.List' - 查找 List 接口的引用\n\n" +
                "📋 支持的类名格式：\n" +
                "• 简单类名：'MyClass'\n" +
                "• 全限定类名：'com.example.MyClass'\n" +
                "• 内部类：'OuterClass.InnerClass'\n" +
                "• 标准库类：'String', 'List', 'Map' 等\n\n" +
                "⚠️ 注意事项：\n" +
                "• 类名区分大小写\n" +
                "• 只查找项目范围内的引用\n" +
                "• 注释和字符串中的引用不会被包含\n"
    }

    /**
     * 根据类名查找 PsiClass
     */
    private fun findClassByName(className: String): PsiClass? {
        println("DEBUG: 查找类名: '$className'")
        
        val scope = GlobalSearchScope.allScope(project)
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        
        // 尝试直接查找
        var psiClass = javaPsiFacade.findClass(className, scope)
        
        if (psiClass == null) {
            // 如果没找到，尝试添加 java.lang 包前缀
            if (!className.contains(".")) {
                val javaLangClass = "java.lang.$className"
                println("DEBUG: 尝试 java.lang 包: '$javaLangClass'")
                psiClass = javaPsiFacade.findClass(javaLangClass, scope)
            }
        }
        
        if (psiClass == null) {
            // 如果还没找到，尝试添加 java.util 包前缀
            if (!className.contains(".")) {
                val javaUtilClass = "java.util.$className"
                println("DEBUG: 尝试 java.util 包: '$javaUtilClass'")
                psiClass = javaPsiFacade.findClass(javaUtilClass, scope)
            }
        }
        
        println("DEBUG: 查找结果: ${psiClass?.qualifiedName ?: "未找到"}")
        return psiClass
    }

    /**
     * 查找指定 PSI 元素的所有引用
     */
    private fun findUsages(element: PsiElement): List<PsiElement> {
        println("DEBUG: 查找元素 '${element.text}' 的引用")
        
        val usages = mutableListOf<PsiElement>()
        val targetName = when (element) {
            is PsiClass -> element.name
            is PsiMethod -> element.name
            is PsiField -> element.name
            is PsiVariable -> element.name
            else -> element.text
        }
        
        if (targetName == null) {
            println("DEBUG: 目标元素名称为空")
            return emptyList()
        }
        
        try {
            // 遍历项目中的所有文件来查找引用
            val scope = GlobalSearchScope.allScope(project)
            val psiManager = PsiManager.getInstance(project)
            
            // 获取项目根目录
            val rootDir = project.baseDir
            collectUsagesFromDirectory(rootDir, targetName, usages, psiManager)
            
            println("DEBUG: 找到 ${usages.size} 个引用")
            return usages
        } catch (e: Exception) {
            println("DEBUG: 查找引用时发生错误: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }
    
    /**
     * 递归遍历目录查找引用
     */
    private fun collectUsagesFromDirectory(directory: VirtualFile, targetName: String, usages: MutableList<PsiElement>, psiManager: PsiManager) {
        for (child in directory.children) {
            if (child.isDirectory) {
                collectUsagesFromDirectory(child, targetName, usages, psiManager)
            } else if (child.extension in listOf("java", "kt", "groovy")) {
                val psiFile = psiManager.findFile(child)
                if (psiFile != null) {
                    collectUsagesFromFile(psiFile, targetName, usages)
                }
            }
        }
    }
    
    /**
     * 从单个文件中查找引用
     */
    private fun collectUsagesFromFile(psiFile: PsiFile, targetName: String, usages: MutableList<PsiElement>) {
        psiFile.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                // 检查元素是否是标识符且名称匹配
                if (element is PsiIdentifier && element.text == targetName) {
                    // 确保这不是定义本身
                    val parent = element.parent
                    if (parent is PsiNameIdentifierOwner && parent.nameIdentifier == element) {
                        // 这是定义，不是引用，跳过
                        return
                    }
                    usages.add(element)
                }
                super.visitElement(element)
            }
        })
    }

    /**
     * 获取元素的行号
     */
    private fun getElementLineNumber(element: PsiElement): Int {
        return try {
            val document = element.containingFile?.viewProvider?.document
            if (document != null) {
                document.getLineNumber(element.textRange.startOffset) + 1
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 获取元素的列号
     */
    private fun getElementColumnNumber(element: PsiElement): Int {
        return try {
            val document = element.containingFile?.viewProvider?.document
            if (document != null) {
                val lineStart = document.getLineStartOffset(document.getLineNumber(element.textRange.startOffset))
                element.textRange.startOffset - lineStart + 1
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 获取元素的上下文信息
     */
    private fun getElementContext(element: PsiElement): String {
        return try {
            val parent = element.parent
            return when {
                parent is PsiClass -> "类定义中"
                parent is PsiMethod -> "方法 '${parent.name}' 中"
                parent is PsiField -> "字段 '${parent.name}' 中"
                parent is PsiVariable -> "变量 '${parent.name}' 中"
                parent is PsiPackage -> "包 '${parent.name}' 中"
                else -> "代码中"
            }
        } catch (e: Exception) {
            "代码中"
        }
    }

    // 测试方法，用于验证 usages 命令匹配
    fun testUsagesCommand(message: String): String {
        println("=== 测试 usages 命令匹配 ===")
        println("输入消息: '$message'")
        
        val result = processMessage(message)
        
        println("=== 测试结果 ===")
        println("输出: $result")
        println("==================")
        
        return result
    }
    
    // 简单的测试方法，用于快速验证 usages 命令
    fun quickTestUsages() {
        val testCases = listOf(
            "usages String",
            "引用 ChatService",
            "用法 java.util.List",
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