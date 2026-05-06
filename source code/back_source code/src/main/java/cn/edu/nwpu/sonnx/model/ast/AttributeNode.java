package cn.edu.nwpu.sonnx.model.ast;

/**
 * 对应语法中的 attribute_def: name_def value_def;
 */
public class AttributeNode extends ASTNode {
    private String name;
    private String value;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}