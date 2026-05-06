package cn.edu.nwpu.sonnx.model.ast;

import java.io.Serializable;

public abstract class ASTNode implements Serializable {
    private String type; // 节点类型名
    private int line;    // 行号，用于错误定位

    public ASTNode() {}

    // 增加这个构造函数，解决 super(String) 报错
    public ASTNode(String type) {
        this.type = type;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }
}