import React, { useState } from 'react';
import {
  Card,
  Tree,
  Typography,
  Space,
  Tag,
  Button,
  Descriptions,
  Form,
  Input,
  Select,
  Modal,
  message,
  Badge,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  BookOutlined,
  ToolOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import type { DataNode } from 'antd/es/tree';
import { IntentKind, IntentLevel } from '@/types/agent';

const { Title, Text } = Typography;

interface IntentNodeData {
  id: string;
  name: string;
  level: IntentLevel;
  kind: IntentKind;
  parentId?: string;
  description?: string;
  mcpToolId?: string;
  collectionName?: string;
}

// Mock intent tree data
const mockIntentTree: IntentNodeData[] = [
  // Domain: 集团信息化
  {
    id: 'group-info',
    name: '集团信息化',
    level: IntentLevel.DOMAIN,
    kind: IntentKind.KB,
    description: '集团内部信息化相关问题',
  },
  {
    id: 'group-hr',
    name: '人事行政',
    level: IntentLevel.CATEGORY,
    kind: IntentKind.KB,
    parentId: 'group-info',
    collectionName: 'kb_group_hr_intro',
    description: '人事流程、规章制度',
  },
  {
    id: 'group-hr-intro',
    name: '员工介绍',
    level: IntentLevel.TOPIC,
    kind: IntentKind.KB,
    parentId: 'group-hr',
    collectionName: 'kb_group_hr_intro',
    description: '新员工入职须知、公司介绍',
  },
  {
    id: 'group-hr-leave',
    name: '请假流程',
    level: IntentLevel.TOPIC,
    kind: IntentKind.KB,
    parentId: 'group-hr',
    collectionName: 'kb_group_hr_leave',
    description: '请假申请、审批流程',
  },
  {
    id: 'group-hr-security',
    name: '安全制度',
    level: IntentLevel.TOPIC,
    kind: IntentKind.KB,
    parentId: 'group-hr',
    collectionName: 'kb_group_hr_security',
    description: '安全操作规程、应急处理',
  },
  // Domain: 业务系统
  {
    id: 'biz-system',
    name: '业务系统',
    level: IntentLevel.DOMAIN,
    kind: IntentKind.KB,
    description: '业务系统使用问题',
  },
  {
    id: 'biz-oa',
    name: 'OA系统',
    level: IntentLevel.CATEGORY,
    kind: IntentKind.KB,
    parentId: 'biz-system',
    collectionName: 'kb_biz_oa_intro',
    description: '办公自动化系统',
  },
  {
    id: 'biz-oa-intro',
    name: '系统介绍',
    level: IntentLevel.TOPIC,
    kind: IntentKind.KB,
    parentId: 'biz-oa',
    collectionName: 'kb_biz_oa_intro',
    description: 'OA系统功能介绍',
  },
  {
    id: 'biz-oa-manual',
    name: '使用手册',
    level: IntentLevel.TOPIC,
    kind: IntentKind.KB,
    parentId: 'biz-oa',
    collectionName: 'kb_biz_oa_manual',
    description: 'OA系统操作手册',
  },
  // Domain: 销售数据 (MCP)
  {
    id: 'sales',
    name: '销售汇总数据',
    level: IntentLevel.DOMAIN,
    kind: IntentKind.MCP,
    description: '销售数据实时查询',
  },
  {
    id: 'sales-data',
    name: '销售数据统计',
    level: IntentLevel.CATEGORY,
    kind: IntentKind.MCP,
    parentId: 'sales',
    mcpToolId: 'sales_query',
    description: '查询销售数据，支持按地区、时间、产品维度统计',
  },
  // Domain: 系统交互
  {
    id: 'system',
    name: '系统交互',
    level: IntentLevel.DOMAIN,
    kind: IntentKind.SYSTEM,
    description: '欢迎语、帮助信息',
  },
  {
    id: 'system-welcome',
    name: '欢迎语',
    level: IntentLevel.TOPIC,
    kind: IntentKind.SYSTEM,
    parentId: 'system',
    description: '标准欢迎回复',
  },
];

function buildTree(nodes: IntentNodeData[]): DataNode[] {
  const map: Record<string, DataNode> = {};
  const roots: DataNode[] = [];

  nodes.forEach((node) => {
    const icon = node.kind === IntentKind.MCP ? (
      <ToolOutlined style={{ color: '#f59e0b' }} />
    ) : node.kind === IntentKind.SYSTEM ? (
      <SettingOutlined style={{ color: '#8c8c8c' }} />
    ) : (
      <BookOutlined style={{ color: '#3b82f6' }} />
    );

    map[node.id] = {
      key: node.id,
      title: (
        <Space size={4}>
          {node.name}
          <Tag
            color={
              node.kind === IntentKind.MCP ? 'orange' :
              node.kind === IntentKind.SYSTEM ? 'default' : 'blue'
            }
            className="text-xs"
          >
            {node.kind}
          </Tag>
          {node.level === IntentLevel.DOMAIN && (
            <Badge status="processing" />
          )}
        </Space>
      ),
      icon,
    };
  });

  nodes.forEach((node) => {
    if (node.parentId) {
      const parent = map[node.parentId];
      if (parent) {
        parent.children = parent.children || [];
        parent.children.push(map[node.id]);
      }
    } else {
      roots.push(map[node.id]);
    }
  });

  return roots;
}

const kindOptions = [
  { label: '知识库 (KB)', value: IntentKind.KB },
  { label: 'MCP 工具 (MCP)', value: IntentKind.MCP },
  { label: '系统交互 (SYSTEM)', value: IntentKind.SYSTEM },
];

const levelOptions = [
  { label: 'Domain (域)', value: IntentLevel.DOMAIN },
  { label: 'Category (类)', value: IntentLevel.CATEGORY },
  { label: 'Topic (主题)', value: IntentLevel.TOPIC },
];

export default function IntentTree() {
  const [selectedNode, setSelectedNode] = useState<IntentNodeData | null>(null);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [, setEditingNode] = useState<IntentNodeData | null>(null);
  const [form] = Form.useForm();
  const [addForm] = Form.useForm();

  const treeData = buildTree(mockIntentTree);

  const handleSelect = (_keys: React.Key[], e: { node: DataNode }) => {
    const nodeId = (e.node as any).key as string;
    const node = mockIntentTree.find((n) => n.id === nodeId);
    setSelectedNode(node || null);
  };

  const handleEdit = () => {
    if (!selectedNode) return;
    setEditingNode(selectedNode);
    form.setFieldsValue(selectedNode);
    setEditModalOpen(true);
  };

  const handleAddChild = () => {
    if (!selectedNode) {
      message.warning('请先选择一个节点');
      return;
    }
    addForm.resetFields();
    addForm.setFieldsValue({ parentId: selectedNode.id, level: IntentLevel.TOPIC });
    setAddModalOpen(true);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Title level={4} className="m-0">意图树编辑器</Title>
        <Space>
          <Button icon={<PlusOutlined />} onClick={() => setAddModalOpen(true)}>
            添加根节点
          </Button>
          <Button icon={<PlusOutlined />} onClick={handleAddChild} disabled={!selectedNode}>
            添加子节点
          </Button>
          <Button icon={<EditOutlined />} onClick={handleEdit} disabled={!selectedNode}>
            编辑节点
          </Button>
          <Button icon={<DeleteOutlined />} danger disabled={!selectedNode}>
            删除
          </Button>
        </Space>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Tree */}
        <Card className="lg:col-span-2" bodyStyle={{ padding: 12 }}>
          <Tree
            showIcon
            showLine={{ showLeafIcon: false }}
            treeData={treeData}
            onSelect={handleSelect}
            defaultExpandAll
            className="intent-tree"
          />
        </Card>

        {/* Node detail */}
        <Card title="节点详情">
          {selectedNode ? (
            <Descriptions column={1} size="small">
              <Descriptions.Item label="节点 ID">
                <Text code>{selectedNode.id}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="名称">
                <Text strong>{selectedNode.name}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="层级">
                <Tag>{selectedNode.level}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="类型">
                <Tag
                  color={
                    selectedNode.kind === IntentKind.MCP ? 'orange' :
                    selectedNode.kind === IntentKind.SYSTEM ? 'default' : 'blue'
                  }
                >
                  {selectedNode.kind}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="描述">
                <Text type="secondary">{selectedNode.description || '—'}</Text>
              </Descriptions.Item>
              {selectedNode.collectionName && (
                <Descriptions.Item label="知识库">
                  <Text code className="text-xs">{selectedNode.collectionName}</Text>
                </Descriptions.Item>
              )}
              {selectedNode.mcpToolId && (
                <Descriptions.Item label="MCP 工具">
                  <Tag color="orange">{selectedNode.mcpToolId}</Tag>
                </Descriptions.Item>
              )}
            </Descriptions>
          ) : (
            <Text type="secondary" className="text-center block py-8">
              点击左侧树节点查看详情
            </Text>
          )}
        </Card>
      </div>

      {/* Legend */}
      <Card size="small">
        <Space size="large">
          <Space size={4}>
            <BookOutlined style={{ color: '#3b82f6' }} />
            <Text type="secondary" className="text-xs">KB 知识库检索</Text>
          </Space>
          <Space size={4}>
            <ToolOutlined style={{ color: '#f59e0b' }} />
            <Text type="secondary" className="text-xs">MCP 工具调用</Text>
          </Space>
          <Space size={4}>
            <SettingOutlined style={{ color: '#8c8c8c' }} />
            <Text type="secondary" className="text-xs">SYSTEM 系统交互</Text>
          </Space>
        </Space>
      </Card>

      {/* Edit Modal */}
      <Modal
        title="编辑节点"
        open={editModalOpen}
        onOk={() => {
          form.validateFields().then(() => {
            message.success('节点已更新');
            setEditModalOpen(false);
          });
        }}
        onCancel={() => setEditModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="id" label="节点 ID" rules={[{ required: true }]}>
            <Input disabled />
          </Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="kind" label="类型" rules={[{ required: true }]}>
            <Select options={kindOptions} />
          </Form.Item>
          <Form.Item name="collectionName" label="知识库集合名">
            <Input placeholder="如: kb_group_hr_intro" />
          </Form.Item>
          <Form.Item name="mcpToolId" label="MCP 工具 ID">
            <Select
              allowClear
              placeholder="选择 MCP 工具"
              options={[
                { label: 'sales_query - 销售数据查询', value: 'sales_query' },
                { label: 'weather_query - 天气查询', value: 'weather_query' },
                { label: 'ticket_query - 工单查询', value: 'ticket_query' },
              ]}
            />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      {/* Add Modal */}
      <Modal
        title="添加节点"
        open={addModalOpen}
        onOk={() => {
          addForm.validateFields().then(() => {
            message.success('节点已添加');
            setAddModalOpen(false);
          });
        }}
        onCancel={() => setAddModalOpen(false)}
      >
        <Form form={addForm} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input placeholder="节点名称" />
          </Form.Item>
          <Form.Item name="parentId" label="父节点">
            <Input disabled placeholder={selectedNode?.name} />
          </Form.Item>
          <Form.Item name="level" label="层级" rules={[{ required: true }]}>
            <Select options={levelOptions} />
          </Form.Item>
          <Form.Item name="kind" label="类型" rules={[{ required: true }]}>
            <Select options={kindOptions} />
          </Form.Item>
          <Form.Item name="collectionName" label="知识库集合名">
            <Input />
          </Form.Item>
          <Form.Item name="mcpToolId" label="MCP 工具 ID">
            <Input placeholder="如: sales_query" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
