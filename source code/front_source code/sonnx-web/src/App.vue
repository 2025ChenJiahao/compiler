<template>
  <el-container style="height: 100vh; background: #f0f2f5;">
    <el-header class="ide-header">
      <div class="logo-area">
        <el-icon :size="25"><Monitor /></el-icon>
        <span class="title">S-ONNX Compiler Professional</span>
      </div>
      <el-button type="primary" color="#cfaf7a" @click="handleCompile" :loading="loading">
        <el-icon><VideoPlay /></el-icon>&nbsp; 运行全流程编译
      </el-button>
    </el-header>

    <el-container>
      <el-main style="padding: 0; background: #1e1e1e;">
        <div id="monaco-editor" style="height: 100%;"></div>
      </el-main>

      <el-aside width="550px" class="side-panel">
        <el-tabs v-model="activeTab" type="border-card">
          
          <el-tab-pane label="词法分析 (Tokens)" name="tokens">
            <div class="result-container">
              <el-table :data="compileResult.tokens" stripe style="width: 100%" height="calc(100vh - 160px)">
                <el-table-column prop="line" label="行号" width="70" />
                <el-table-column prop="type" label="Token 类型" width="160">
                  <template #default="scope">
                    <el-tag size="small" effect="plain">{{ scope.row.type }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="text" label="原始文本" />
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="AST 树" name="ast">
            <div class="code-container">
              <el-tag effect="dark" size="small" style="margin-bottom: 10px;">Syntax Tree (JSON)</el-tag>
              <pre class="json-view">{{ astDisplay }}</pre>
            </div>
          </el-tab-pane>

          <el-tab-pane label="符号表详情" name="symbol">
            <div class="result-container">
              <div v-if="compileResult.symbolTable.length === 0" class="empty-msg">暂无符号数据</div>
              <div v-for="(item, i) in compileResult.symbolTable" :key="i" class="symbol-row">
                <el-icon><CollectionTag /></el-icon> {{ item }}
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="TAC 中间代码" name="tac">
            <div class="tac-container">
              <div v-if="!compileResult.success && compileResult.errors.length > 0">
                <el-alert 
                  v-for="(err, i) in compileResult.errors" :key="i"
                  :title="`第 ${err.line} 行：${err.type}`"
                  type="error"
                  :description="err.message"
                  show-icon
                  class="error-item"
                />
              </div>
              <div v-else>
                <div v-if="!compileResult.tac" class="empty-msg">等待生成三地址码...</div>
                <pre class="tac-view">{{ compileResult.tac }}</pre>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="编译日志" name="log">
            <div class="log-view">
              <div v-for="(msg, i) in logs" :key="i" class="log-item">{{ msg }}</div>
            </div>
          </el-tab-pane>

        </el-tabs>
      </el-aside>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import * as monaco from 'monaco-editor'

// UI 状态
const activeTab = ref('tokens') 
const loading = ref(false)
const logs = ref(['> IDE Initialized.'])

// 响应式数据源：严格对应后端 CompileResult.java 结构
const compileResult = ref({
  success: true,
  tokens: [],
  ast: null,
  symbolTable: [], 
  tac: "",         
  errors: []
})

let editor = null

// 计算属性：美化 AST JSON 展示
const astDisplay = computed(() => {
  return compileResult.value.ast 
    ? JSON.stringify(compileResult.value.ast, null, 2) 
    : '暂无数据'
})

onMounted(() => {
  // 初始化 Monaco Editor
  editor = monaco.editor.create(document.getElementById('monaco-editor'), {
    value: 'graph "Example" {\n  input { x = float32[1, 3] }\n  node "Relu_1" ("Relu") ["x"] -> ["y"]\n}',
    language: 'javascript',
    theme: 'vs-dark',
    automaticLayout: true,
    fontSize: 14,
    minimap: { enabled: false }
  })
})

const handleCompile = async () => {
  loading.value = true
  const startTime = new Date().toLocaleTimeString()
  logs.value.push(`> Start compiling at ${startTime}`)
  
  try {
    const response = await fetch('http://localhost:8080/api/compiler/compile', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code: editor.getValue() })
    })
    
    if (!response.ok) throw new Error('Network response was not ok')
    
    const result = await response.json()
    
    // 更新响应式数据
    compileResult.value.success = result.success
    compileResult.value.tokens = result.tokens || []
    compileResult.value.ast = result.ast
    compileResult.value.symbolTable = result.symbolTable || [] 
    compileResult.value.tac = result.tac || ""                 
    compileResult.value.errors = result.errors || []

    if (result.success) {
      logs.value.push('> Semantic analysis passed.')
      logs.value.push('> TAC generation completed.')
      logs.value.push('> All stages completed successfully.')
      activeTab.value = 'tac' // 成功后自动跳转到代码展示
    } else {
      logs.value.push(`> Compilation failed with ${result.errors.length} error(s).`)
      activeTab.value = 'tac' // 失败后跳转到 TAC 栏查看报错 Alert
    }
  } catch (e) {
    logs.value.push('> Error: Connection to backend failed. Please check if SpringBoot is running.')
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 布局与头部 */
.ide-header { 
  background: #002147; 
  color: #fff; 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  border-bottom: 2px solid #cfaf7a; 
}
.logo-area { display: flex; align-items: center; gap: 10px; }
.title { font-weight: bold; font-size: 1.2rem; letter-spacing: 1px; }

/* 右侧面板 */
.side-panel { border-left: 1px solid #dcdfe6; }
.result-container, .code-container, .tac-container, .log-view { 
  height: calc(100vh - 160px); 
  overflow: auto; 
  padding: 12px; 
}

/* 文本展示区域 (AST & TAC) */
.json-view, .tac-view { 
  background: #f8f9fa; 
  padding: 15px; 
  font-size: 13px; 
  border: 1px solid #ebeef5; 
  white-space: pre-wrap; 
  word-wrap: break-word;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  line-height: 1.6;
  border-radius: 4px;
}
.tac-view { 
  color: #d73a49; 
  font-weight: bold; 
  background: #fffcfc; 
  border-left: 4px solid #cfaf7a;
}

/* 符号表样式 */
.symbol-row {
  padding: 10px 15px;
  border-bottom: 1px solid #ebeef5;
  font-family: 'Consolas', monospace;
  color: #409EFF;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  margin-bottom: 4px;
  border-radius: 4px;
  transition: background 0.3s;
}
.symbol-row:hover { background: #ecf5ff; }

/* 错误与日志 */
.error-item { margin-bottom: 12px; }
.log-view { 
  background: #1a1a1a; 
  color: #67C23A; 
  font-family: 'Courier New', monospace; 
  font-size: 13px; 
  line-height: 1.8;
}
.log-item { border-bottom: 1px solid #333; padding: 2px 0; }
.empty-msg { 
  color: #909399; 
  text-align: center; 
  margin-top: 60px; 
  font-style: italic;
}

/* 覆盖 Element Plus Tabs 样式使之更紧凑 */
:deep(.el-tabs--border-card) {
  border: none;
  box-shadow: none;
}
</style>