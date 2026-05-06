package cn.edu.nwpu.sonnx.dto;

import cn.edu.nwpu.sonnx.core.error.CompileError;
import cn.edu.nwpu.sonnx.model.ast.ModelNode;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================
 * S-ONNX 编译器 统一编译结果DTO
 * 整合全流程编译产物，满足实验输出要求：
 * 1. 编译成功状态
 * 2. AST抽象语法树
 * 3. 三地址码(TAC)中间代码
 * 4. 全量错误列表（词法/语法/语义）
 * 5. 词法Token流
 * 6. 语义分析符号表
 * ================================
 */
public class CompileResult {
    private boolean success;          // 编译是否成功（无任何错误为true）
    private ModelNode ast;            // 生成的AST根节点
    private String tac;               // 生成的三地址码（字符串格式）
    private List<CompileError> errors;// 所有错误（词法+语法+语义）
    private List<TokenInfo> tokens;   // 词法分析生成的Token流
    private List<String> symbolTable; // 语义分析生成的符号表

    // 初始化默认：编译失败
    public CompileResult() {
        this.success = false;
        this.errors = new ArrayList<>();
        this.tokens = new ArrayList<>();
        this.symbolTable = new ArrayList<>();
    }

    /**
     * 统一添加错误方法
     * 自动将编译状态设为失败，对接语义分析/词法/语法错误
     * @param type 错误类型
     * @param line 错误行号
     * @param message 错误描述
     */
    public void addError(String type, int line, String message) {
        this.errors.add(new CompileError(type, line, message));
        this.success = false;
    }

    /**
     * 批量添加错误（对接ANTLR错误监听器）
     * @param errorList 词法/语法错误列表
     */
    public void addErrors(List<CompileError> errorList) {
        if (errorList != null && !errorList.isEmpty()) {
            this.errors.addAll(errorList);
            this.success = false;
        }
    }

    // ================================
    // Token 内部类：存储词法Token信息
    // 用于展示词法分析结果
    // ================================
    public static class TokenInfo {
        private int line;    // Token所在行号
        private String type; // Token类型（如STRING/INTEGER/IDENTIFIER）
        private String text; // Token文本内容

        public TokenInfo(int line, String type, String text) {
            this.line = line;
            this.type = type;
            this.text = text;
        }

        // getter/setter
        public int getLine() { return line; }
        public void setLine(int line) { this.line = line; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    // ================================
    // 标准 getter/setter
    // 对接编译器全流程模块
    // ================================
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public ModelNode getAst() { return ast; }
    public void setAst(ModelNode ast) { this.ast = ast; }

    public String getTac() { return tac; }
    public void setTac(String tac) { this.tac = tac; }

    public List<CompileError> getErrors() { return errors; }
    public void setErrors(List<CompileError> errors) {
        this.errors = errors;
        // 有错误则编译失败
        if (errors != null && !errors.isEmpty()) {
            this.success = false;
        }
    }

    public List<TokenInfo> getTokens() { return tokens; }
    public void setTokens(List<TokenInfo> tokens) { this.tokens = tokens; }

    public List<String> getSymbolTable() { return symbolTable; }
    public void setSymbolTable(List<String> symbolTable) { this.symbolTable = symbolTable; }
}