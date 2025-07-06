// 简单的tools测试脚本
import { LangGraphAgentService } from './services/langGraphAgentService';

async function testTools() {
  console.log('🧪 开始测试 Tools...');
  
  try {
    // 初始化agent
    await LangGraphAgentService.initialize();
    console.log('✅ Agent 初始化成功');
    
    // 测试用例
    const testCases = [
      "今天天气怎么样？",
      "现在几点了？", 
      "请计算 2 + 3 * 4",
      "有什么新闻吗？"
    ];
    
    for (const testCase of testCases) {
      console.log(`\n🔍 测试: "${testCase}"`);
      try {
        const response = await LangGraphAgentService.sendMessage(testCase);
        console.log(`✅ 响应: ${response}`);
      } catch (error) {
        console.error(`❌ 错误: ${error.message}`);
      }
    }
    
  } catch (error) {
    console.error('❌ 测试失败:', error);
  }
}

// 如果直接运行此文件
if (typeof window !== 'undefined') {
  // 在浏览器环境中
  window.testTools = testTools;
  console.log('🔧 Tools测试函数已加载到全局作用域，可以在控制台运行: testTools()');
}

export { testTools }; 