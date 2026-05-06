package cn.edu.nwpu.sonnx.model.ast;

import java.util.ArrayList;
import java.util.List;

public class NodeNode extends ASTNode {
    private String opType;
    private String name;
    private List<String> inputs = new ArrayList<>();
    private List<String> outputs = new ArrayList<>();

    public NodeNode() { super("Node"); }
    // --- 必须添加以下内容来解决报错 ---
    private List<AttributeNode> attributes = new ArrayList<>();

    public List<AttributeNode> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeNode> attributes) {
        this.attributes = attributes;
    }
    // Getters and Setters
    public String getOpType() { return opType; }
    public void setOpType(String opType) { this.opType = opType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getInputs() { return inputs; }
    public List<String> getOutputs() { return outputs; }
}