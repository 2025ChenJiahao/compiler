package cn.edu.nwpu.sonnx.model.ast;
import java.util.ArrayList;
import java.util.List;

public class GraphNode extends ASTNode {
    private String name;
    private List<NodeNode> nodes = new ArrayList<>();
    private List<ValueInfoNode> inputs = new ArrayList<>();
    private List<ValueInfoNode> outputs = new ArrayList<>();
    private List<TensorNode> initializers = new ArrayList<>();

    public GraphNode() { super("Graph"); }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<NodeNode> getNodes() { return nodes; }
    public List<ValueInfoNode> getInputs() { return inputs; }
    public List<ValueInfoNode> getOutputs() { return outputs; }
    public List<TensorNode> getInitializers() { return initializers; }
}