import { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  Typography,
  Progress,
  Badge,
  message,
  Divider,
  Collapse,
  Row,
  Col,
  Statistic,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { PromptVersion, TuningResult, VersionStatus } from '@/types/prompt';
import { VersionStatus as VS_VAL, TuningStrategy as TS_VAL } from '@/types/prompt';
import {
  PlusOutlined,
  RocketOutlined,
  EditOutlined,
  HistoryOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';

const { Title, Text } = Typography;
const { TextArea } = Input;
const { Panel } = Collapse;

const mockVersions: PromptVersion[] = [
  {
    versionId: 'prompt-qa-v1',
    promptId: 'prompt-qa',
    content: '你是一个企业知识库问答助手。请根据提供的上下文信息，准确回答用户问题。如果上下文中没有相关信息，请如实告知。',
    description: '初始版本',
    versionNumber: 1,
    status: VS_VAL.PUBLISHED,
    createdTime: Date.now() - 86400000 * 7,
    publishedTime: Date.now() - 86400000 * 6,
    metrics: { evalScore: 72.5, responseQuality: 70, tokenCost: 120, iterationCount: 1, successRate: 0.85 },
  },
  {
    versionId: 'prompt-qa-v2',
    promptId: 'prompt-qa',
    content: '你是一个企业知识库问答助手。请仔细阅读上下文，理解用户问题，从上下文中提取关键信息给出准确答案。回答要简洁、专业、有条理。',
    description: '增加结构化要求',
    versionNumber: 2,
    status: VS_VAL.PUBLISHED,
    createdTime: Date.now() - 86400000 * 3,
    publishedTime: Date.now() - 86400000 * 2,
    metrics: { evalScore: 81.3, responseQuality: 79, tokenCost: 135, iterationCount: 1, successRate: 0.91 },
  },
  {
    versionId: 'prompt-qa-v3',
    promptId: 'prompt-qa',
    content: '',
    description: '待优化版本',
    versionNumber: 3,
    status: VS_VAL.DRAFT,
    createdTime: Date.now() - 3600000,
    metrics: { evalScore: 0, responseQuality: 0, tokenCost: 0, iterationCount: 0, successRate: 0 },
  },
];

const mockTuningIterations = [
  {
    iterationNumber: 1,
    variants: [
      { variantId: 'v1', content: '...（添加了角色定义）', changeType: 'GRAMMATICAL', score: 75.2 },
      { variantId: 'v2', content: '...（添加了格式要求）', changeType: 'STRUCTURAL', score: 78.8 },
      { variantId: 'v3', content: '...（简化了措辞）', changeType: 'CONCISENESS', score: 73.1 },
    ],
    bestVariant: { variantId: 'v2', content: '...', changeType: 'STRUCTURAL', score: 78.8 },
    bestScore: 78.8,
    improvement: 6.3,
  },
  {
    iterationNumber: 2,
    variants: [
      { variantId: 'v1', content: '...（基于v2添加示例）', changeType: 'EXAMPLES', score: 82.4 },
      { variantId: 'v2', content: '...（基于v2精简）', changeType: 'CONCISENESS', score: 80.1 },
      { variantId: 'v3', content: '...（综合优化）', changeType: 'HYBRID', score: 83.7 },
    ],
    bestVariant: { variantId: 'v3', content: '...', changeType: 'HYBRID', score: 83.7 },
    bestScore: 83.7,
    improvement: 4.9,
  },
];

const strategyOptions = [
  { label: '语法/措辞 (GRAMMATICAL)', value: TS_VAL.GRAMMATICAL },
  { label: '结构重组 (STRUCTURAL)', value: TS_VAL.STRUCTURAL },
  { label: '精简优化 (CONCISENESS)', value: TS_VAL.CONCISENESS },
  { label: '示例增强 (EXAMPLES)', value: TS_VAL.EXAMPLES },
  { label: '综合策略 (HYBRID)', value: TS_VAL.HYBRID },
];

const statusColor: Record<VersionStatus, string> = {
  DRAFT: 'default',
  TESTING: 'processing',
  PUBLISHED: 'success',
  ROLLED_BACK: 'warning',
  ARCHIVED: 'default',
};

export default function PromptTuningPage() {
  const [versions, setVersions] = useState<PromptVersion[]>(mockVersions);
  const [_selectedVersion, setSelectedVersion] = useState<PromptVersion | null>(null);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [optimizeModalOpen, setOptimizeModalOpen] = useState(false);
  const [optimizing, setOptimizing] = useState(false);
  const [tuningResult, setTuningResult] = useState<TuningResult | null>(null);
  const [iterations, _setIterations] = useState(mockTuningIterations);
  const [form] = Form.useForm();

  const handleCreate = (values: any) => {
    const newVersion: PromptVersion = {
      versionId: `prompt-qa-v${versions.length + 1}`,
      promptId: values.promptId || 'prompt-qa',
      content: values.content,
      description: values.description,
      versionNumber: versions.length + 1,
      status: VS_VAL.DRAFT,
      createdTime: Date.now(),
      metrics: { evalScore: 0, responseQuality: 0, tokenCost: 0, iterationCount: 0, successRate: 0 },
    };
    setVersions((prev) => [...prev, newVersion]);
    setCreateModalOpen(false);
    message.success('版本已创建');
  };

  const handleOptimize = async () => {
    setOptimizing(true);
    // Simulate optimization
    await new Promise((r) => setTimeout(r, 3000));
    setTuningResult({
      promptId: 'prompt-qa',
      initialContent: versions[versions.length - 1]?.content || '',
      initialScore: 72.5,
      finalContent: '优化后的 Prompt 内容...',
      finalScore: 83.7,
      totalImprovement: 11.2,
      iterations: 2,
      successful: true,
      startTime: Date.now() - 3000,
      endTime: Date.now(),
    });
    setOptimizing(false);
    message.success('Prompt 优化完成');
  };

  const columns: ColumnsType<PromptVersion> = [
    {
      title: '版本',
      dataIndex: 'versionNumber',
      key: 'version',
      width: 80,
      render: (n, record) => (
        <Space>
          <Tag color={record.status === VS_VAL.PUBLISHED ? 'green' : 'default'}>
            v{n}
          </Tag>
        </Space>
      ),
    },
    {
      title: '内容预览',
      dataIndex: 'content',
      key: 'content',
      render: (text) => (
        <Text className="text-xs line-clamp-2" style={{ display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {text || <Text type="secondary">（空）</Text>}
        </Text>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (s: string) => <Badge status={statusColor[s as keyof typeof statusColor] as any} text={<span className="text-xs">{s}</span>} />,
    },
    {
      title: '评测分数',
      dataIndex: ['metrics', 'evalScore'],
      key: 'evalScore',
      width: 100,
      render: (score) => (
        score > 0 ? (
          <Progress
            percent={Math.round(score)}
            size="small"
            strokeColor={score >= 80 ? '#52c41a' : score >= 60 ? '#faad14' : '#ff4d4f'}
            format={() => score.toFixed(0)}
          />
        ) : '—'
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 160,
      render: (ts: number) => new Date(ts).toLocaleString('zh-CN'),
    },
    {
      title: '操作',
      key: 'action',
      width: 160,
      render: (_, record) => (
        <Space size="small">
          <Button size="small" type="link" icon={<EditOutlined />} onClick={() => setSelectedVersion(record)}>
            编辑
          </Button>
          {record.status === VS_VAL.DRAFT && (
            <Button size="small" type="link" icon={<RocketOutlined />} onClick={() => {
              setVersions(prev => prev.map(v => v.versionId === record.versionId ? { ...v, status: VS_VAL.PUBLISHED } : v));
              message.success('已发布');
            }}>
              发布
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Title level={4} className="m-0">Prompt 调优</Title>
        <Space>
          <Button icon={<ThunderboltOutlined />} onClick={() => setOptimizeModalOpen(true)}>
            自动优化
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalOpen(true)}>
            新建版本
          </Button>
        </Space>
      </div>

      {/* Version list */}
      <Table
        columns={columns}
        dataSource={versions}
        rowKey="versionId"
        size="small"
        pagination={{ pageSize: 10 }}
      />

      {/* Tuning history */}
      {tuningResult && (
        <Card title={<Space><HistoryOutlined /><span>优化过程</span></Space>} size="small">
          <Collapse defaultActiveKey={['1', '2']}>
            {iterations.map((iter) => (
              <Panel
                key={iter.iterationNumber}
                header={
                  <Space>
                    <Tag>迭代 {iter.iterationNumber}</Tag>
                    <Text>最佳分数: <Text strong>{iter.bestScore}</Text></Text>
                    <Text type="success">+{iter.improvement} 分</Text>
                    <Tag color="blue">{iter.bestVariant.changeType}</Tag>
                  </Space>
                }
              >
                <Table
                  size="small"
                  dataSource={iter.variants}
                  rowKey="variantId"
                  pagination={false}
                  columns={[
                    { title: '变体', dataIndex: 'changeType', render: t => <Tag>{t}</Tag> },
                    { title: '评测分数', dataIndex: 'score', render: s => <Text strong>{s}</Text> },
                    { title: '内容', dataIndex: 'content', render: t => <Text className="text-xs">{t}</Text> },
                  ]}
                />
              </Panel>
            ))}
          </Collapse>

          <Divider />
          <Row gutter={16}>
            <Col span={6}><Statistic title="初始分数" value={tuningResult.initialScore} /></Col>
            <Col span={6}><Statistic title="最终分数" value={tuningResult.finalScore} valueStyle={{ color: '#52c41a' }} /></Col>
            <Col span={6}><Statistic title="提升" value={`+${tuningResult.totalImprovement}`} valueStyle={{ color: '#52c41a' }} /></Col>
            <Col span={6}><Statistic title="迭代次数" value={tuningResult.iterations} /></Col>
          </Row>
        </Card>
      )}

      {/* Optimize modal */}
      <Modal
        title="Prompt 自动优化"
        open={optimizeModalOpen}
        onCancel={() => { setOptimizeModalOpen(false); setTuningResult(null); }}
        footer={[
          <Button key="cancel" onClick={() => setOptimizeModalOpen(false)}>取消</Button>,
          <Button key="optimize" type="primary" loading={optimizing} onClick={handleOptimize} icon={<ThunderboltOutlined />}>
            {optimizing ? '优化中...' : '开始优化'}
          </Button>,
        ]}
        width={600}
      >
        <Space direction="vertical" className="w-full">
          <Form.Item label="优化策略">
            <Select mode="multiple" options={strategyOptions} placeholder="选择优化策略（可多选）" />
          </Form.Item>
          <Form.Item label="评测用例">
            <Select
              mode="multiple"
              placeholder="选择评测用例"
              options={[
                { label: '简单问答 (3个)', value: 'simple-qa' },
                { label: '工具调用 (2个)', value: 'tool-call' },
                { label: '推理 (3个)', value: 'reasoning' },
                { label: '拒绝 (2个)', value: 'reject' },
                { label: 'RAG (2个)', value: 'rag' },
              ]}
            />
          </Form.Item>
          <Text type="secondary" className="text-xs">
            优化将基于选定的策略和评测用例，自动生成 Prompt 变体并评估效果。
          </Text>
        </Space>
      </Modal>

      {/* Create modal */}
      <Modal
        title="新建 Prompt 版本"
        open={createModalOpen}
        onOk={() => form.submit()}
        onCancel={() => setCreateModalOpen(false)}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleCreate}
          initialValues={{ promptId: 'prompt-qa', content: '', description: '' }}
        >
          <Form.Item name="promptId" label="Prompt ID" rules={[{ required: true }]}>
            <Input placeholder="prompt-qa" />
          </Form.Item>
          <Form.Item name="content" label="Prompt 内容" rules={[{ required: true }]}>
            <TextArea rows={6} placeholder="输入 Prompt 内容..." />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input placeholder="版本描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
