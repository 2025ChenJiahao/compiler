package cn.edu.nwpu.sonnx.model.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * 张量信息节点 (ValueInfoNode)
 * 职责：描述模型中输入 (Input) 和输出 (Output) 的元数据，包括名称、数据类型和形状（Shape）。
 */
public class ValueInfoNode extends ASTNode {
    private String name;           // 张量名称
    private String elemType;       // 元素类型: int, float, bool, string
    private List<Integer> shape = new ArrayList<>(); // 张量维度信息

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getElemType() { return elemType; }
    public void setElemType(String elemType) { this.elemType = elemType; }

    public List<Integer> getShape() { return shape; }
    public void setShape(List<Integer> shape) { this.shape = shape; }
}