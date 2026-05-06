package cn.edu.nwpu.sonnx.core.semantic;

import cn.edu.nwpu.sonnx.model.ast.ValueInfoNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 通用符号表工具类
 * 作用：提供基础符号表增删查、类型校验工具方法
 * 配合 SemanticAnalyzer 共同完成语义检查
 * 校验范围：张量定义、节点定义、类型比对
 */
public class SymbolTable {

    // 存储所有张量：输入/输出/初始化器（Key=名称, Value=类型信息）
    private final Map<String, ValueInfoNode> tensorSymbols = new HashMap<>();
    // 存储节点名称（Key=节点名, Value=行号）
    private final Map<String, Integer> nodeSymbols = new HashMap<>();

    // =============================================
    // 1. 命名冲突检查 工具方法
    // =============================================
    /**
     * 添加张量 → 自动检查重名（输入/输出/初始化器共用，禁止重名）
     */
    public boolean addTensor(String name, ValueInfoNode info) {
        if (tensorSymbols.containsKey(name)) {
            return false;
        }
        tensorSymbols.put(name, info);
        return true;
    }

    /**
     * 添加节点 → 自动检查节点重名
     */
    public boolean addNode(String name, int line) {
        if (nodeSymbols.containsKey(name)) {
            return false;
        }
        nodeSymbols.put(name, line);
        return true;
    }

    // =============================================
    // 2. 未定义即使用检查 工具方法
    // =============================================
    /**
     * 检查张量/初始化器是否已定义
     */
    public boolean isTensorDefined(String name) {
        return tensorSymbols.containsKey(name);
    }

    /**
     * 查找张量信息（为空=未定义）
     */
    public Optional<ValueInfoNode> lookupTensor(String name) {
        return Optional.ofNullable(tensorSymbols.get(name));
    }

    // =============================================
    // 3. 张量类型检查 工具方法
    // =============================================
    /**
     * 检查一组输入张量类型完全一致
     * 用于二元/多元算子类型校验
     */
    public boolean checkAllInputsSameType(String... tensorNames) {
        if (tensorNames == null || tensorNames.length <= 1) {
            return true;
        }
        String firstType = tensorSymbols.get(tensorNames[0]).getElemType();
        for (String name : tensorNames) {
            ValueInfoNode tensor = tensorSymbols.get(name);
            if (tensor == null || !tensor.getElemType().equals(firstType)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 校验单个张量是否为指定类型
     */
    public boolean checkTensorType(String tensorName, String expectedType) {
        ValueInfoNode tensor = tensorSymbols.get(tensorName);
        return tensor != null && tensor.getElemType().equals(expectedType);
    }

    // =============================================
    // 工具方法
    // =============================================
    /**
     * 清空符号表，支持多文件解析
     */
    public void clear() {
        tensorSymbols.clear();
        nodeSymbols.clear();
    }

    /**
     * 判断节点名是否已存在
     */
    public boolean containsNode(String name) {
        return nodeSymbols.containsKey(name);
    }
}