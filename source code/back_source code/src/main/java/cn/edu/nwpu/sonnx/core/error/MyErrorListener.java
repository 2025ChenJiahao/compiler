package cn.edu.nwpu.sonnx.core.error;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================
 * S-ONNX 编译器 词法/语法错误监听器
 * 基于ANTLR实现，自动捕获编译前端错误
 * 对应实验任务：
 * 1. 自动区分词法/语法错误
 * 2. 错误定位：行号+字符位置+错误代码片段
 * 3. 错误收集：统一管理所有前端错误
 * ================================
 */
public class MyErrorListener extends BaseErrorListener {
    // 存储所有捕获的词法/语法错误
    private final List<CompileError> errors = new ArrayList<>();

    /**
     * ANTLR 核心错误回调方法
     * 自动捕获：词法错误、语法错误
     */
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        // 1. 自动分类：词法错误 OR 语法错误
        String errorType = (e instanceof org.antlr.v4.runtime.LexerNoViableAltException)
                ? "Lexical"  // 词法错误：非法字符、字符串未闭合
                : "Syntax";  // 语法错误：括号缺失、结构不匹配

        // 2. 错误定位：获取出错的代码片段（满足实验要求）
        String codeSnippet = "";
        if (offendingSymbol instanceof Token token) {
            codeSnippet = "，错误代码：'" + token.getText() + "'";
        }

        // 3. 拼接完整错误信息（位置+原因+代码片段）
        String fullMessage = "字符位置 " + charPositionInLine + ": " + msg + codeSnippet;
        errors.add(new CompileError(errorType, line, fullMessage));
    }

    /**
     * 获取所有词法+语法错误
     * 用于传递给 CompileResult 统一返回
     */
    public List<CompileError> getErrors() {
        return errors;
    }

    /**
     * 判断是否存在编译错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 清空错误列表（支持多次编译）
     */
    public void clearErrors() {
        errors.clear();
    }
}