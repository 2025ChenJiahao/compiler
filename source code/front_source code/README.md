# S-ONNXCompiler 前端项目 (front)

## 项目概述

front 是 S-ONNXCompiler 编译器项目的前端部分，包含两个主要模块：
- **sonnx-web**：基于 Vue 3 + Vite 的编译器 IDE 主界面
- **ASTtree/tools**：AST 语法树可视化工具

## 目录结构

```
front/
├── README.md                          # 项目说明文档
├── bat/                               # 批处理脚本目录
│   ├── start_frontend.bat            # 启动 sonnx-web 前端
│   └── start_tool.bat                # 启动 AST 可视化工具
├── ASTtree/                           # AST 语法树相关
│   ├── Ast_tree_file/                # AST 树数据文件
│   │   ├── 1.txt
│   │   ├── 2.txt
│   │   ├── 3.txt
│   │   ├── 4.txt
│   │   ├── 5.txt
│   │   └── 6.txt
│   └── tools/                        # AST 可视化工具
│       └── tool.html                 # 基于 Mermaid 的 AST 可视化页面
└── sonnx-web/                        # 主前端应用 (Vue 3 + Vite)
    ├── .gitignore                    # Git 忽略配置
    ├── .vscode/                      # VSCode 配置
    │   └── extensions.json           # 推荐的 VSCode 扩展 (Vue.volar)
    ├── index.html                    # HTML 入口文件
    ├── package.json                  # 项目依赖配置
    ├── package-lock.json             # 依赖版本锁定文件
    ├── vite.config.js                # Vite 构建配置
    ├── public/                       # 静态公共资源
    │   ├── favicon.svg               # 网站图标
    │   └── icons.svg                 # 图标精灵文件
    └── src/                          # 源代码目录
        ├── App.vue                   # Vue 根组件（编译器IDE主界面）
        ├── main.js                   # Vue 应用入口
        ├── style.css                 # 全局样式文件
        ├── assets/                   # 静态资源
        │   ├── hero.png              # Hero 图片
        │   ├── vite.svg             # Vite Logo
        │   └── vue.svg              # Vue Logo
        └── components/              # Vue 组件目录
            └── HelloWorld.vue       # 示例组件（Vite 默认模板）
```

## 技术栈

### sonnx-web 主应用
- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 新一代前端构建工具
- **Element Plus** - 基于 Vue 3 的组件库
- **Monaco Editor** - 代码编辑器组件（VS Code 同款）
- **@element-plus/icons-vue** - Element Plus 图标库

### ASTtree 工具
- **Tailwind CSS** - Tailwind CSS 框架
- **Mermaid** - 流程图/语法树渲染库
- **Lucide** - 图标库

## 快速启动

### 启动 sonnx-web 主前端

**方式一：使用批处理脚本**
```batch
.\bat\start_frontend.bat
```

**方式二：手动启动**
```bash
cd sonnx-web
npm install
npm run dev
```

启动后访问 http://localhost:5173 或 http://localhost:3000

### 启动 AST 可视化工具

**方式一：使用批处理脚本**
```batch
.\bat\start_tool.bat
```

**方式二：手动打开**
直接在浏览器中打开以下文件：
```
ASTtree\tools\tool.html
```

## sonnx-web 功能说明

### 主界面布局
- **顶部导航栏**：包含 Logo 和"运行全流程编译"按钮
- **左侧主区域**：Monaco 代码编辑器，用于编写 ONNX 编译器代码
- **右侧面板**：包含 5 个标签页

### 右侧面板标签页

1. **词法分析 (Tokens)**
   - 显示词法分析结果
   - 包含行号、Token 类型、原始文本三列

2. **AST 树**
   - 以 JSON 格式展示抽象语法树

3. **符号表详情**
   - 显示编译过程中的符号表信息

4. **TAC 中间代码**
   - 显示三地址码（Three-Address Code）
   - 编译错误信息也会显示在此处

5. **编译日志**
   - 实时显示编译过程的日志信息

### 与后端通信
- 编译按钮点击后，向 `http://localhost:8080/api/compiler/compile` 发送 POST 请求
- 请求体格式：`{ code: "编辑器中的代码" }`
- 响应数据结构对应 `CompileResult.java`

## ASTtree/tool.html 功能说明

### 主要功能
- **JSON AST 输入**：左侧面板可粘贴或输入 JSON 格式的 AST 数据
- **语法树可视化**：右侧画布使用 Mermaid 渲染 AST 语法树
- **缩放控制**：支持缩放和平移操作

### 操作按钮
- **FORMAT**：格式化 JSON 输入
- **CLEAR**：清空输入
- **+/-**：缩放控制
- **刷新图标**：重置缩放和平移
- **居中图标**：将图形居中显示

### 技术实现
- 使用 Mermaid 的 flowchart 渲染 AST
- 支持递归遍历 JSON 对象生成 Mermaid DSL
- 拖拽平移和滚轮缩放

## npm 脚本说明

在 `sonnx-web` 目录下可使用以下命令：

```bash
npm run dev      # 启动开发服务器
npm run build     # 构建生产版本
npm run preview   # 预览生产构建结果
```

## 依赖说明

### 生产依赖
- `vue` ^3.5.32 - Vue 3 核心库
- `element-plus` ^2.13.7 - UI 组件库
- `@element-plus/icons-vue` ^2.3.2 - Element Plus 图标
- `monaco-editor` ^0.55.1 - 代码编辑器
- `@rolldown/binding-win32-x64-msvc` ^1.0.0-rc.17 - Rolldown Windows 绑定

### 开发依赖
- `vite` ^8.0.10 - 构建工具
- `@vitejs/plugin-vue` ^6.0.6 - Vue 插件

## 注意事项

1. 启动前端前请确保后端 Spring Boot 服务已运行（端口 8080）
2. 前端默认连接后端地址：`http://localhost:8080/api/compiler/compile`
3. AST 可视化工具是独立的 HTML 文件，无需后端服务

## 开发工具推荐

- **VSCode** + **Vue.volar** 扩展（已在 `.vscode/extensions.json` 中推荐）