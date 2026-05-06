# S-ONNXCompiler测试目录

## 目录结构

本目录包含 S-ONNXCompiler项目的测试用例和分析结果。

```
test_txt/
├── Test_file/
│   ├── Ast_tree_file/
│   │   ├── mermaid_tree/  # AST 树的 Mermaid 图表图片（7和8没有通过语法分析没有AST树）
│   │   ├── 1.txt  # 测试用例 1 的 AST 树文本
│   │   ├── 2.txt  # 测试用例 2 的 AST 树文本
│   │   └── ...  # 其他测试用例的 AST 树文本
│   └── test_1-10_files/
│       ├── test1.txt  # 测试用例 1 的源代码
│       ├── test2.txt  # 测试用例 2 的源代码
│       └── ...  # 其他测试用例的源代码
├── test_1/  # 测试用例 1 的分析结果
│   ├── Lexical Analysis/  # 词法分析结果
│   ├── Syntax Analysis/  # 语法分析结果
│   ├── Semantic Analysis/  # 语义分析结果
│   └── Three-Address Code (TAC)/  # 三地址码生成结果
├── test_2/  # 测试用例 2 的分析结果
│   └── ...  # 与 test_1 结构相同
└── ...  # 其他测试用例的分析结果
```

## 测试用例说明

本目录包含 10 个测试用例（test_1 到 test_10），每个测试用例都包含以下分析结果：

1. **词法分析（Lexical Analysis）**：
   - PNG 图片
   - 展示了源代码的词法分析结果，包括 token 识别

2. **语法分析（Syntax Analysis）**：
   - PNG 图片
   - 展示了源代码的语法分析结果，包括语法树构建

3. **语义分析（Semantic Analysis）**：
   - PNG 图片
   - 展示了源代码的语义分析结果，包括类型检查等

4. **三地址码生成（Three-Address Code）**：
   - PNG 图片
   - 展示了源代码的三地址码生成结果

## AST 树

在 `Test_file/Ast_tree_file/` 目录中：
- `*.txt` 文件：包含各测试用例的 AST 树文本表示
- `mermaid_tree/` 目录：包含各测试用例的 AST 树 Mermaid 图表图片

## 测试源代码

在 `Test_file/test_1-10_files/` 目录中包含了所有测试用例的源代码文件（test1.txt 到 test10.txt）。

## 使用方法

1. 查看测试源代码：打开 `Test_file/test_1-10_files/` 目录下的对应测试文件
2. 查看分析结果：进入对应测试用例目录（如 test_1/），然后查看各分析类型的结果
3. 查看 AST 树：可以查看 `Test_file/Ast_tree_file/` 目录下的文本表示或 Mermaid 图表

## 注意事项

- 本目录仅包含测试用例和分析结果，不包含编译器后端的核心代码
- 分析结果以图片形式展示，便于直观理解编译过程的各个阶段
