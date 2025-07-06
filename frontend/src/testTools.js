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
        let response = "";
        let toolCallCount = 0;
        
        await LangGraphAgentService.sendMessageWithStreaming(
          testCase,
          (chunk) => { 
            response = chunk; 
            console.log(`📝 Chunk received: ${chunk}`);
          },
          (final) => { 
            response = final; 
            console.log(`✅ Final response: ${final}`);
          },
          (toolCall) => {
            toolCallCount++;
            console.log(`🔧 Tool call ${toolCallCount}:`, toolCall);
          }
        );
        console.log(`✅ Response: ${response}`);
        console.log(`🔧 Tool calls made: ${toolCallCount}`);
      } catch (error) {
        console.error(`❌ 错误: ${error.message}`);
      }
    }
    
  } catch (error) {
    console.error('❌ 测试失败:', error);
  }
}

// 工具调用演示函数
function demoToolCall() {
  console.log('🎬 开始工具调用演示...');
  
  // 模拟工具调用流程
  const demoSteps = [
    {
      step: 1,
      action: '用户发送消息',
      message: '今天天气怎么样？'
    },
    {
      step: 2,
      action: 'AI决定使用工具',
      tool: 'search'
    },
    {
      step: 3,
      action: '工具调用执行',
      input: { query: '今天天气怎么样？' },
      output: '今天天气晴朗，温度25度，适合外出活动。'
    },
    {
      step: 4,
      action: '显示工具调用信息',
      display: '在对话中显示工具调用详情'
    }
  ];
  
  demoSteps.forEach(step => {
    console.log(`步骤 ${step.step}: ${step.action}`);
    if (step.message) console.log(`  消息: "${step.message}"`);
    if (step.tool) console.log(`  工具: ${step.tool}`);
    if (step.input) console.log(`  输入: ${JSON.stringify(step.input)}`);
    if (step.output) console.log(`  输出: ${step.output}`);
    if (step.display) console.log(`  显示: ${step.display}`);
    console.log('');
  });
  
  console.log('✅ 工具调用演示完成');
}

// 如果直接运行此文件
if (typeof window !== 'undefined') {
  // 在浏览器环境中
  window.testTools = testTools;
  window.demoToolCall = demoToolCall;
  console.log('🔧 Tools测试函数已加载到全局作用域，可以在控制台运行: testTools() 或 demoToolCall()');
}

// 测试工具调用功能
console.log('🧪 Testing tool call functionality...');

// 模拟测试工具调用
const testToolCall = {
  toolName: 'search',
  toolInput: { query: '今天天气怎么样？' },
  toolOutput: '今天天气晴朗，温度25度，适合外出活动。'
};

console.log('🔧 Tool call test data:', testToolCall);

// 测试消息格式
const testMessage = {
  id: 'test-1',
  content: '🔧 使用了 search 工具\n输入: {"query":"今天天气怎么样？"}\n输出: 今天天气晴朗，温度25度，适合外出活动。',
  sender: 'assistant',
  toolCall: {
    toolName: 'search',
    toolInput: { query: '今天天气怎么样？' },
    toolOutput: '今天天气晴朗，温度25度，适合外出活动。',
    timestamp: new Date().toISOString()
  }
};

console.log('📝 Test message format:', testMessage);
console.log('✅ Tool call test setup complete');

// 导出测试函数
export { testTools, demoToolCall }; 