package cn.edu.nwpu.sonnx.core.parser;

import cn.edu.nwpu.sonnx.generated.SONNXBaseVisitor;
import cn.edu.nwpu.sonnx.generated.SONNXParser;
import cn.edu.nwpu.sonnx.model.ast.*;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * S-ONNX 抽象语法树 (AST) 构建器
 * 采用 Visitor 模式遍历 ANTLR 生成的 ParseTree，并将其转换为自定义的 AST 节点。
 * * 核心逻辑：
 * 1. 适配 .g4 语法中的 repeats 结构（处理重复出现的节点、输入、输出等）。
 * 2. 清理原始 Token 中的引号和多余字符。
 * 3. 记录行号信息，为后续语义分析中的错误定位提供依据。
 * ============================================================
 */
public class ASTBuilderVisitor extends SONNXBaseVisitor<ASTNode> {

    // ====================== 1. Model (模型顶层) ======================
    /**
     * 访问 Model 根节点。
     * 对应语法：model: MODELPROTO '{' model_body_def '}'
     */
    @Override
    public ASTNode visitModel(SONNXParser.ModelContext ctx) {
        ModelNode model = new ModelNode();
        // 记录起始行号，用于报错时定位到 ModelProto 这一行
        model.setLine(ctx.getStart().getLine());

        // 按照新语法层级递归访问：model -> model_body_def -> graph_def
        if (ctx.model_body_def() != null && ctx.model_body_def().graph_def() != null) {
            // visit() 会自动分发到 visitGraph_def 并返回构建好的 GraphNode
            model.setGraph((GraphNode) visit(ctx.model_body_def().graph_def()));
        }
        return model;
    }

    // ====================== 2. Graph (计算图) ======================
    /**
     * 访问 Graph 定义。
     * 对应语法：graph_def: GRAPH '{' graph_body_def '}'
     * 处理重点：循环遍历 node_list, input_list 等 repeats 结构。
     */
    @Override
    public ASTNode visitGraph_def(SONNXParser.Graph_defContext ctx) {
        GraphNode graph = new GraphNode();
        graph.setLine(ctx.getStart().getLine());
        var body = ctx.graph_body_def();

        if (body != null) {
            // 解析图名称，并去掉 STRING Token 带的双引号
            graph.setName(cleanQuotes(body.name_def().STRING().getText()));

            // --- 处理节点列表 (node_list) ---
            // 由于语法定义为 node_repeats+，生成类中对应的是列表结构
            if (body.node_list() != null) {
                for (var repeat : body.node_list().node_repeats()) {
                    // 递归构建每个 node 并加入 graph 集合
                    graph.getNodes().add((NodeNode) visit(repeat.node_def()));
                }
            }

            // --- 处理输入列表 (input_list) ---
            // 解决之前 tree is null 的关键点：必须通过 repeats 容器获取内容
            if (body.input_list() != null) {
                for (var repeat : body.input_list().input_repeats()) {
                    graph.getInputs().add((ValueInfoNode) visit(repeat.value_info_def()));
                }
            }

            // --- 处理输出列表 (output_list) ---
            if (body.output_list() != null) {
                for (var repeat : body.output_list().output_repeats()) {
                    graph.getOutputs().add((ValueInfoNode) visit(repeat.value_info_def()));
                }
            }

            // --- 处理初始化器 (initializer_list) ---
            // 注意：初始化器在语法中是可选的 (initializer_list?)
            if (body.initializer_list() != null) {
                for (var repeat : body.initializer_list().initializer_repeats()) {
                    graph.getInitializers().add((TensorNode) visit(repeat.tensor_def()));
                }
            }
        }
        return graph;
    }

    // ====================== 3. Node (计算节点) ======================
    /**
     * 访问 Node 定义。
     * 对应语法：node_def: op_type_def name_def input_side? output_side? attribute_list?
     * 处理重点：兼容数组式输入 ["a", "b"] 和 列表式输入 input{...}。
     */
    @Override
    public ASTNode visitNode_def(SONNXParser.Node_defContext ctx) {
        NodeNode node = new NodeNode();
        node.setLine(ctx.getStart().getLine());

        // 提取算子类型 (如 "Add", "Conv") 和 节点实例名称
        node.setOpType(cleanQuotes(ctx.op_type_def().STRING().getText()));
        node.setName(cleanQuotes(ctx.name_def().STRING().getText()));

        // --- 处理输入端 (Input Side) ---
        // 适配两种情况：
        // 1. input = ["a", "b"] (input_arr)
        // 2. input { name="a" ... } (input_list)
        if (ctx.input_arr() != null) {
            for (TerminalNode s : ctx.input_arr().STRING()) {
                node.getInputs().add(cleanQuotes(s.getText()));
            }
        } else if (ctx.input_list() != null) {
            for (var repeat : ctx.input_list().input_repeats()) {
                // 对于列表式输入，提取其内部 ValueInfo 的名字
                node.getInputs().add(cleanQuotes(repeat.value_info_def().name_def().STRING().getText()));
            }
        }

        // --- 处理输出端 (Output Side) ---
        if (ctx.output_arr() != null) {
            for (TerminalNode s : ctx.output_arr().STRING()) {
                node.getOutputs().add(cleanQuotes(s.getText()));
            }
        } else if (ctx.output_list() != null) {
            for (var repeat : ctx.output_list().output_repeats()) {
                node.getOutputs().add(cleanQuotes(repeat.value_info_def().name_def().STRING().getText()));
            }
        }

        // --- 处理属性列表 (attribute_list) ---
        // 遍历所有 attribute_repeats 并手动填充 AttributeNode 实体
        if (ctx.attribute_list() != null) {
            for (var repeat : ctx.attribute_list().attribute_repeats()) {
                AttributeNode attr = new AttributeNode();
                attr.setName(cleanQuotes(repeat.attribute_def().name_def().STRING().getText()));
                attr.setValue(cleanQuotes(repeat.attribute_def().value_def().STRING().getText()));
                node.getAttributes().add(attr);
            }
        }
        return node;
    }

    // ====================== 4. ValueInfo (张量信息) ======================
    /**
     * 访问输入/输出的类型和维度定义。
     * 处理重点：从 elem_type = float 中提取 float，解析 shape 列表。
     */
    @Override
    public ASTNode visitValue_info_def(SONNXParser.Value_info_defContext ctx) {
        ValueInfoNode info = new ValueInfoNode();
        info.setName(cleanQuotes(ctx.name_def().STRING().getText()));

        // 如果定义了类型块 (type_def -> tensor_type_def)
        if (ctx.type_def() != null) {
            var tensorType = ctx.type_def().tensor_type_def();

            // 提取数据类型：由于文本可能是 "elem_type = float"，需要按 "=" 切分
            String elemText = tensorType.elem_type_def().getText();
            info.setElemType(elemText.contains("=") ? elemText.split("=")[1].trim() : elemText);

            // 解析维度信息 (Shape)
            if (tensorType.shape_def() != null) {
                // 遍历 dim_list 下的每一个 dim_repeats
                for (var repeat : tensorType.shape_def().dim_list().dim_repeats()) {
                    var dim = repeat.dim_def();
                    // 如果是固定数值 (dim_value = 1)
                    if (dim.DIM_VALUE() != null) {
                        info.getShape().add(Integer.parseInt(dim.INTEGER().getText()));
                    } else {
                        // 如果是动态参数 (dim_param)，在 AST 中通常记为 -1 表示不确定
                        info.getShape().add(-1);
                    }
                }
            }
        }
        return info;
    }

    // ====================== 5. Tensor (张量/初始化器数据) ======================
    /**
     * 访问初始化器定义。
     * 处理重点：提取数据类型、维度列表以及原始二进制数据。
     */
    @Override
    public ASTNode visitTensor_def(SONNXParser.Tensor_defContext ctx) {
        TensorNode tensor = new TensorNode();
        tensor.setName(cleanQuotes(ctx.name_def().STRING().getText()));

        // 提取数据类型 (如 float, int32)
        String typeText = ctx.data_type_def().getText();
        tensor.setDataType(typeText.contains("=") ? typeText.split("=")[1].trim() : typeText);

        // 适配新规则：dims = 1 2 3 (直接是 INTEGER 列表，不再有中括号)
        if (ctx.dims_def() != null) {
            for (TerminalNode intNode : ctx.dims_def().INTEGER()) {
                tensor.getDims().add(Integer.parseInt(intNode.getText()));
            }
        }

        // 提取二进制原始数据块
        if (ctx.raw_data_def() != null) {
            tensor.setRawData(ctx.raw_data_def().BYTES().getText());
        }
        return tensor;
    }

    /**
     * 工具方法：清理字符串。
     * 去除 Token 文本两端的双引号并进行 trim。
     */
    private String cleanQuotes(String s) {
        return s != null ? s.replace("\"", "").trim() : "";
    }
}