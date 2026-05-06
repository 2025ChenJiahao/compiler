package cn.edu.nwpu.sonnx.core.error;

/**
 * ================================
 * S-ONNX 编译器 统一错误实体类
 * 对应实验任务：错误分类 + 错误报告机制
 * 支持三类错误：
 * 1. Lexical：词法错误（非法字符、字符串未闭合等）
 * 2. Syntax：语法错误（缺少符号、结构不匹配等）
 * 3. Semantic：语义错误（命名冲突、未定义引用、类型不匹配等）
 * ================================
 */
public class CompileError {
    private String type;    // 错误类型（固定：Lexical/Syntax/Semantic）
    private int line;       // 错误行号（错误定位核心字段）
    private String message; // 错误详细描述（错误原因+代码片段）

    // 无参构造：用于序列化/反序列化
    public CompileError() {
    }

    /**
     * 全参构造：快速创建错误对象
     * @param type 错误类型
     * @param line 错误行号
     * @param message 错误描述
     */
    public CompileError(String type, int line, String message) {
        this.type = type;
        this.line = line;
        this.message = message;
    }

    // ================= getter/setter =================
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 标准化错误输出格式
     * 满足实验：错误描述清晰、可直接打印展示
     * 输出示例：[Semantic Error] Line 10: 节点名称重复定义: AddNode
     */
    @Override
    public String toString() {
        return String.format("[%s Error] Line %d: %s", type, line, message);
    }
}