package cn.edu.nwpu.sonnx.model.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * 权重张量节点 (TensorNode)
 * 职责：对应 S-ONNX 中的 Initializer 结构，存储模型的常量权重信息。
 */
public class TensorNode extends ASTNode {
    private String name;           // 张量名称
    private String dataType;       // 数据类型 (如: FLOAT, INT32)
    private List<Integer> dims = new ArrayList<>(); // 维度信息 (Shape)
    private String rawData;        // 原始字节数据 (以字符串形式存储)
    public TensorNode() { super("TensorInitializer"); }
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public List<Integer> getDims() { return dims; }
    public void setDims(List<Integer> dims) { this.dims = dims; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
}