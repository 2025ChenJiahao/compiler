grammar SONNX;
// 包声明（保持原有）
@header {
package cn.edu.nwpu.sonnx.generated;
}
// 开启【关键字不区分大小写】（ANTLR标准配置）
options {
    caseInsensitive = true;
}
// ---------------- 语法规则 (Parser Rules) 【保留原分类 + 严格匹配G[model]】 ----------------
// 固定顺序：版本→生产者名→版本→域名→模型版本→文档→图→算子集
// 顶级模型-------专用符号：{ }
model: MODELPROTO '{' model_body_def '}';
model_body_def: ir_version_def producer_name_def producer_version_def domain_def model_version_def doc_string_def graph_def opset_import_def;

// 模型基础属性-------专用符号：=
ir_version_def: IR_VERSION '=' INTEGER;
producer_name_def: PRODUCER_NAME '=' STRING;
producer_version_def: PRODUCER_VERSION '=' STRING;
domain_def: DOMAIN '=' STRING;
model_version_def: MODEL_VERSION '=' INTEGER;
doc_string_def: DOC_STRING '=' STRING;

// 计算图-------专用符号：{ }
graph_def: GRAPH '{' graph_body_def '}';
// 固定顺序：名称→节点→输入→输出 + 可选初始化器
graph_body_def: name_def node_list input_list output_list initializer_list?;
// 赋值--------------专用符号：=
name_def: NAME '=' STRING;

// 计算节点
node_list: node_repeats+; // 匹配BNF：重复节点
node_repeats: NODE '{' node_def '}';// 专用符号：{ }
// 严格匹配BNF：算子类型+名称+(输入列表/数组)+(输出列表/数组)+可选属性
node_def: op_type_def name_def (input_list | input_arr) (output_list | output_arr) attribute_list?;
op_type_def: OP_TYPE '=' STRING;// 专用符号：=

// 节点输入/输出（数组格式）// 输入输出数组  // 专用符号：=  [  ]  ,
input_arr: INPUT '=' '[' (STRING (',' STRING)*)? ']';
output_arr: OUTPUT '=' '[' (STRING (',' STRING)*)? ']';

// 图输入/输出（匹配BNF：重复定义）// 结构化输入输出  // 专用符号：{ }
input_list: input_repeats+;
input_repeats: INPUT '{' value_info_def '}';
output_list: output_repeats+;
output_repeats: OUTPUT '{' value_info_def '}';

// 初始化器（权重）// 专用符号：{ }
initializer_list: initializer_repeats+; // 匹配BNF：重复初始化器
initializer_repeats: INITIALIZER '{' tensor_def '}';

// 节点属性// 专用符号：{ }
attribute_list: attribute_repeats+; // 匹配BNF：重复属性
attribute_repeats: ATTRIBUTE '{' attribute_def '}';
attribute_def: name_def value_def;
value_def: VALUE '=' STRING;// 专用符号：=

// 张量类型定义（命名严格匹配BNF）// 专用符号：{ }
value_info_def: name_def type_def;
type_def: TYPE '{' tensor_type_def '}';
tensor_type_def: TENSOR_TYPE '{' elem_type_def shape_def '}';
elem_type_def: ELEM_TYPE '=' (INT_T | FLOAT_T | STRING_T | BOOL_T);// 专用符号：=

// 张量形状// 维度结构  // 专用符号：{ }
shape_def: SHAPE '{' dim_list '}';
dim_list: dim_repeats+; // 匹配BNF：重复维度
dim_repeats: DIM '{' dim_def '}';
dim_def: DIM_VALUE '=' INTEGER | DIM_PARAM '=' STRING;

// 张量数据定义
tensor_def: name_def data_type_def dims_def raw_data_def;
data_type_def: DATA_TYPE '=' (INT_T | FLOAT_T | STRING_T | BOOL_T);// 专用符号：=
// 严格匹配BNF：仅支持空格分隔整数，删除[]格式
dims_def: DIMS '=' INTEGER+;// 专用符号：=
raw_data_def: RAW_DATA '=' BYTES;// 专用符号：=

// 算子集导入（固定顺序）
opset_import_def: OPSET_IMPORT '{' domain_def version_def '}';// 专用符号：{ }
version_def: VERSION '=' INTEGER;// 专用符号：=

// ------------------------- 词法规则 (Lexer Rules) --------------------------------
// =============================================
// 1. 32个关键字【严格按你提供的顺序 + 不区分大小写】
// =============================================
MODELPROTO     : 'ModelProto';
GRAPH          : 'graph';
NAME           : 'name';
NODE           : 'node';
INPUT          : 'input';
OUTPUT         : 'output';
OP_TYPE        : 'op_type';
ATTRIBUTE      : 'attribute';
INITIALIZER    : 'initializer';
DOC_STRING     : 'doc_string';
DOMAIN         : 'domain';
MODEL_VERSION  : 'model_version';
PRODUCER_NAME  : 'producer_name';
PRODUCER_VERSION: 'producer_version';
TYPE           : 'type';
TENSOR_TYPE    : 'tensor_type';
IR_VERSION     : 'ir_version';
ELEM_TYPE      : 'elem_type';
SHAPE          : 'shape';
DIM            : 'dim';
DIMS           : 'dims';
RAW_DATA       : 'raw_data';
OPSET_IMPORT   : 'opset_import';
DIM_VALUE      : 'dim_value';
DIM_PARAM      : 'dim_param';
DATA_TYPE      : 'data_type';
VERSION        : 'version';
VALUE          : 'value';
INT_T          : 'int';
FLOAT_T        : 'float';
STRING_T       : 'string';
BOOL_T         : 'bool';


// ---------------- 基础数据类型正则（严格按文档） ----------------
// 整数后缀
fragment INTEGER_TYPE_SUFFIX : 'l' | 'L';
// 整数规则
INTEGER : ('0' | [1-9][0-9]*) INTEGER_TYPE_SUFFIX?;

// 转义字符
fragment ESCAPE_SEQUENCE : '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\');
// 字符串规则
STRING : '"' ( ESCAPE_SEQUENCE | ~('\\'|'"') )* '"';

// 字节数据
BYTES : [0-9A-Fa-f]+ 'b';

// 忽略空白、注释
WS: [ \t\r\n]+ -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;