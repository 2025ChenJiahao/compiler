package cn.edu.nwpu.sonnx.model.ast;

public class ModelNode extends ASTNode {
    private GraphNode graph;
    public ModelNode() { super("ModelProto"); }
    public GraphNode getGraph() { return graph; }
    public void setGraph(GraphNode graph) { this.graph = graph; }
}