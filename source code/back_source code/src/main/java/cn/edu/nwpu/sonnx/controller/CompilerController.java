package cn.edu.nwpu.sonnx.controller;

import cn.edu.nwpu.sonnx.dto.CompileResult;
import cn.edu.nwpu.sonnx.core.error.CompileError;
import cn.edu.nwpu.sonnx.core.error.MyErrorListener;
import cn.edu.nwpu.sonnx.core.parser.ASTBuilderVisitor;
import cn.edu.nwpu.sonnx.core.semantic.SemanticAnalyzer;
import cn.edu.nwpu.sonnx.core.tac.TACGenerator;
import cn.edu.nwpu.sonnx.model.ast.ModelNode;
import cn.edu.nwpu.sonnx.generated.SONNXLexer;
import cn.edu.nwpu.sonnx.generated.SONNXParser;
import org.antlr.v4.runtime.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/compiler")
public class CompilerController {

    // 内部类：接收前端发送的源码请求
    public static class SourceCodeRequest {
        private String code;
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    /**
     * S-ONNX 编译核心方法
     * 严格执行五个部分逻辑：Token流 -> AST构建 -> 语义分析 -> 符号表填充 -> TAC生成
     */
    @PostMapping("/compile")
    public CompileResult compileModel(@RequestBody SourceCodeRequest request) {
        CompileResult result = new CompileResult();
        String code = request.getCode();

        try {
            // ====================== 部分 1. 词法分析 (Token展示) ======================
            SONNXLexer lexerForView = new SONNXLexer(CharStreams.fromString(code));
            CommonTokenStream tokenStream = new CommonTokenStream(lexerForView);
            tokenStream.fill();
            List<CompileResult.TokenInfo> tokenInfos = new ArrayList<>();
            for (Token t : tokenStream.getTokens()) {
                if (t.getType() != Token.EOF) {
                    tokenInfos.add(new CompileResult.TokenInfo(t.getLine(),
                            SONNXLexer.VOCABULARY.getSymbolicName(t.getType()), t.getText()));
                }
            }
            result.setTokens(tokenInfos);

            // ====================== 部分 2. 语法分析 (AST构建) ======================
            MyErrorListener errorListener = new MyErrorListener();
            SONNXLexer lexer = new SONNXLexer(CharStreams.fromString(code));
            lexer.removeErrorListeners(); // 移除控制台默认输出
            lexer.addErrorListener(errorListener);

            SONNXParser parser = new SONNXParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // 【核心修正点】：先获取 Context，不直接 visit
            SONNXParser.ModelContext modelCtx = parser.model();

            // 如果存在词法或语法错误，立即停止并返回，防止 tree is null 崩溃
            if (!errorListener.getErrors().isEmpty()) {
                result.setErrors(errorListener.getErrors());
                result.setSuccess(false);
                return result;
            }

            // 只有语法正确，才执行 Visitor 构建 AST
            ASTBuilderVisitor visitor = new ASTBuilderVisitor();
            ModelNode ast = (ModelNode) visitor.visit(modelCtx);
            result.setAst(ast);

            // ====================== 部分 3 & 4. 语义分析 & 符号表填充 ======================
            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            // 语义分析逻辑：检查命名冲突、未定义引用等
            List<CompileError> sErrors = analyzer.analyze(ast);

            // 填充符号表输出 (即使有语义错误，也展示已扫描到的符号)
            result.setSymbolTable(analyzer.getSymbolTableOutput());

            // 如果存在语义错误，返回错误列表
            if (sErrors != null && !sErrors.isEmpty()) {
                result.setErrors(sErrors);
                result.setSuccess(false);
                return result;
            }

            // ====================== 部分 5. TAC 生成 ======================
            TACGenerator tacGen = new TACGenerator();
            List<String> tacLines = tacGen.generate(ast);
            result.setTac(String.join("\n", tacLines));

            // 全流程成功
            result.setSuccess(true);

        } catch (Exception e) {
            // 捕获异常，防止 Internal 0 错误导致前端无法解析
            e.printStackTrace();
            result.addError("Internal", 0, "编译器运行异常: " + e.getMessage());
        }
        return result;
    }
}