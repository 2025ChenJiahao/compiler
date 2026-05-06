package cn.edu.nwpu.sonnx.core.semantic;

import cn.edu.nwpu.sonnx.model.ast.*;
import cn.edu.nwpu.sonnx.core.error.CompileError;

import java.util.*;

/**
 * 语义分析器（增强版）
 * 修复了初始化器冲突逻辑，并优化了类型校验与报错信息
 */
public class SemanticAnalyzer {
    private final List<CompileError> errors = new ArrayList<>();

    // 全局张量符号表：存储所有输入/输出/初始化器张量
    private final Map<String, ValueInfoNode> tensorSymbolTable = new HashMap<>();
    // 节点名称集合：检测计算节点命名冲突
    private final Set<String> nodeNameSet = new HashSet<>();
    // 已由计算节点产出的张量集合
    private final Set<String> nodeProducedTensors = new HashSet<>();

    public List<CompileError> analyze(ModelNode model) {
        if (model == null || model.getGraph() == null) {
            return errors;
        }

        GraphNode graph = model.getGraph();
        tensorSymbolTable.clear();
        nodeNameSet.clear();
        nodeProducedTensors.clear();

        // ==========================================
        // 第一阶段：注册图的输入 (Inputs)
        // ==========================================
        if (graph.getInputs() != null) {
            for (ValueInfoNode input : graph.getInputs()) {
                if (input.getName() != null) {
                    // 所有的 input 都是合法的符号源头
                    tensorSymbolTable.put(input.getName(), input);
                }
            }
        }

        // ==========================================
        // 第二阶段：注册初始化器 (Initializers)
        // 注意：初始化器可以与 Input 同名，不应报错，而是视为该 Input 拥有了具体值
        // ==========================================
        if (graph.getInitializers() != null) {
            for (TensorNode init : graph.getInitializers()) {
                String name = init.getName();
                if (name == null) continue;

                ValueInfoNode v = new ValueInfoNode();
                v.setName(name);
                v.setElemType(cleanType(init.getDataType()));
                v.setShape(init.getDims());

                // 如果符号表已存在该名（来自Input），则更新信息；否则新增
                tensorSymbolTable.put(name, v);
            }
        }

        // ==========================================
        // 第三阶段：遍历计算节点 (Nodes)
        // ==========================================
        if (graph.getNodes() != null) {
            for (NodeNode node : graph.getNodes()) {
                if (node == null) continue;

                // 1. 检查节点重名
                if (node.getName() != null && !node.getName().isEmpty()) {
                    if (nodeNameSet.contains(node.getName())) {
                        errors.add(new CompileError("Semantic", node.getLine(), "计算节点名称重复定义: " + node.getName()));
                    }
                    nodeNameSet.add(node.getName());
                }

                // 2. 检查输入是否存在 + 类型一致性校验
                String inferredType = "float";
                boolean isFirstInput = true;

                if (node.getInputs() != null && !node.getInputs().isEmpty()) {
                    for (String inName : node.getInputs()) {
                        if (!tensorSymbolTable.containsKey(inName)) {
                            errors.add(new CompileError("Semantic", node.getLine(),
                                    String.format("算子 [%s] 引用了未定义的张量: %s", node.getOpType(), inName)));
                        } else {
                            // 类型校验
                            String currentType = cleanType(tensorSymbolTable.get(inName).getElemType());
                            if (isFirstInput) {
                                inferredType = currentType;
                                isFirstInput = false;
                            } else {
                                if (currentType != null && !currentType.equalsIgnoreCase(inferredType)) {
                                    errors.add(new CompileError("Semantic", node.getLine(),
                                            String.format("算子 [%s] 输入类型不一致: %s 期待 %s, 但遇到 %s",
                                                    node.getOpType(), inName, inferredType, currentType)));
                                }
                            }
                        }
                    }
                }

                // 3. 注册输出张量
                if (node.getOutputs() != null) {
                    for (String outName : node.getOutputs()) {
                        // 检查是否被多个节点产出（ONNX 不允许 SSA 冲突）
                        if (nodeProducedTensors.contains(outName)) {
                            errors.add(new CompileError("Semantic", node.getLine(),
                                    "张量输出冲突，多个节点产出了同名张量: " + outName));
                        } else {
                            ValueInfoNode outInfo = new ValueInfoNode();
                            outInfo.setName(outName);
                            outInfo.setElemType(inferredType);
                            // 将产出的张量加入符号表供下游节点使用
                            tensorSymbolTable.put(outName, outInfo);
                            nodeProducedTensors.add(outName);
                        }
                    }
                }
            }
        }

        // ==========================================
        // 第四阶段：模型输出 (Graph Outputs) 合法性校验
        // ==========================================
        if (graph.getOutputs() != null) {
            for (ValueInfoNode graphOutput : graph.getOutputs()) {
                String outName = graphOutput.getName();
                // 检查最终输出是否由 Input 或 Node 产出
                if (!tensorSymbolTable.containsKey(outName)) {
                    errors.add(new CompileError("Semantic", graphOutput.getLine(),
                            "图定义的输出张量不存在于任何算子产出中: " + outName));
                }
            }
        }

        return errors;
    }

    /**
     * 清洗类型字符串，去除可能存在的引号和空格
     */
    private String cleanType(String type) {
        if (type == null) return "float";
        return type.replace("\"", "").replace("'", "").trim().toLowerCase();
    }

    public List<String> getSymbolTableOutput() {
        List<String> output = new ArrayList<>();
        // 使用 TreeMap 保证输出顺序一致，方便调试
        Map<String, ValueInfoNode> sortedMap = new TreeMap<>(tensorSymbolTable);
        for (Map.Entry<String, ValueInfoNode> entry : sortedMap.entrySet()) {
            ValueInfoNode info = entry.getValue();
            String row = String.format("[%s] 类型: %s, 维度: %s",
                    entry.getKey(),
                    info.getElemType(),
                    info.getShape() != null ? info.getShape().toString() : "[]");
            output.add(row);
        }
        return output;
    }
}