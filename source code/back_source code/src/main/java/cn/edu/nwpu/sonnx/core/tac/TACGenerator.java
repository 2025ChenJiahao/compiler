package cn.edu.nwpu.sonnx.core.tac;

import cn.edu.nwpu.sonnx.model.ast.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 严格规范版 三地址码 TAC 生成器
 * 1. 输入定义：T1 = Input("input1", FLOAT, [1, 3, 224, 224])
 * 2. 权重初始化：W1 = Initializer("weight1", FLOAT, [64, 3, 3, 3], raw_data=...)
 * 3. 算子操作：T3 = Conv2d(input1, W1, kernel_shape=[3,3])
 * 4. 输出定义：Output("output1", T2)
 */
public class TACGenerator {
    private final List<String> tacList = new ArrayList<>();

    // 中间变量计数器
    private int tCount = 1;
    private int wCount = 1;

    // 映射表：[AST张量名] -> [TAC中间变量名 T1/W1]
    private final Map<String, String> nameToTacVar = new HashMap<>();

    public List<String> generate(ModelNode model) {
        if (model == null || model.getGraph() == null) return tacList;

        GraphNode graph = model.getGraph();
        tacList.clear();
        tCount = 1;
        wCount = 1;
        nameToTacVar.clear();

        // --- 1. 输入张量定义 ---
        // 格式：T1 = Input("name", FLOAT, [shape])
        if (graph.getInputs() != null) {
            for (ValueInfoNode input : graph.getInputs()) {
                String varName = "T" + (tCount++);
                nameToTacVar.put(input.getName(), varName);

                String tac = String.format("%s = Input(\"%s\", %s, %s)",
                        varName,
                        input.getName(),
                        input.getElemType().toUpperCase(),
                        input.getShape().toString());
                tacList.add(tac);
            }
        }

        // --- 2. 权重初始化 ---
        // 格式：W1 = Initializer("name", FLOAT, [shape], raw_data=...)
        if (graph.getInitializers() != null) {
            for (TensorNode init : graph.getInitializers()) {
                String varName = "W" + (wCount++);
                nameToTacVar.put(init.getName(), varName);

                String rawDataPreview = init.getRawData() != null ? init.getRawData() : "null";
                String tac = String.format("%s = Initializer(\"%s\", %s, %s, raw_data=%s)",
                        varName,
                        init.getName(),
                        init.getDataType().toUpperCase(),
                        init.getDims().toString(),
                        rawDataPreview);
                tacList.add(tac);
            }
        }

        // --- 3. 算子操作 ---
        // 格式：T4 = Relu(T3) 或 T3 = Conv(T1, W1, attr=val)
        if (graph.getNodes() != null) {
            for (NodeNode node : graph.getNodes()) {
                // 转换输入张量为 TAC 变量
                List<String> operands = new ArrayList<>();
                for (String in : node.getInputs()) {
                    operands.add(nameToTacVar.getOrDefault(in, in));
                }

                // 处理节点属性（如果有）
                if (node.getAttributes() != null && !node.getAttributes().isEmpty()) {
                    for (AttributeNode attr : node.getAttributes()) {
                        operands.add(attr.getName() + "=" + attr.getValue());
                    }
                }

                // 为每个输出生成中间变量
                List<String> results = new ArrayList<>();
                for (String out : node.getOutputs()) {
                    String varName = "T" + (tCount++);
                    nameToTacVar.put(out, varName);
                    results.add(varName);
                }

                String tac = String.format("%s = %s(%s)",
                        String.join(", ", results),
                        node.getOpType(),
                        String.join(", ", operands));
                tacList.add(tac);
            }
        }

        // --- 4. 输出张量定义 ---
        // 格式：Output("name", T2)
        if (graph.getOutputs() != null) {
            for (ValueInfoNode output : graph.getOutputs()) {
                String sourceVar = nameToTacVar.getOrDefault(output.getName(), output.getName());
                String tac = String.format("Output(\"%s\", %s)",
                        output.getName(),
                        sourceVar);
                tacList.add(tac);
            }
        }

        return tacList;
    }
}