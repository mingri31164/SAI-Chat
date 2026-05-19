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
  Slider,
  Typography,
  message,
  Tooltip,
  Badge,
  Progress,
  Statistic,
  Row,
  Col,
} from 'antd';
import {
  PlusOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  DeleteOutlined,
  BarChartOutlined,
  CheckCircleFilled,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { TestStatus } from '@/types/abtest';
import type { ABTest, ABVariant, VariantResult } from '@/types/abtest';

const { Title, Text } = Typography;

const mockTests: ABTest[] = [
  {
    testId: 'test-001',
    name: 'RAG 检索策略对比测试',
    description: '对比向量检索与意图导向检索的效果差异',
    status: TestStatus.RUNNING,
    variantCount: 2,
    variantWeights: [0.5, 0.5],
    minSignificance: 0.95,
    minSampleSize: 1000,
    createdTime: Date.now() - 86400000 * 3,
    startTime: Date.now() - 86400000 * 2,
  },
  {
    testId: 'test-002',
    name: 'Prompt 模板优化测试',
    description: '对比不同 Prompt 模板的问答质量',
    status: TestStatus.DRAFT,
    variantCount: 3,
    variantWeights: [0.4, 0.3, 0.3],
    createdTime: Date.now() - 3600000,
  },
  {
    testId: 'test-003',
    name: '深思考模式效果评估',
    description: '评估开启/关闭 deepThinking 对回答质量的影响',
    status: TestStatus.COMPLETED,
    variantCount: 2,
    variantWeights: [0.5, 0.5],
    createdTime: Date.now() - 86400000 * 7,
    startTime: Date.now() - 86400000 * 6,
    endTime: Date.now() - 86400000 * 2,
    winnerVariantId: 'test-003-v1',
  },
];

const mockVariants: Record<string, ABVariant[]> = {
  'test-001': [
    { variantId: 'test-001-v0', variantName: 'control', weight: 0.5, isControl: true },
    { variantId: 'test-001-v1', variantName: 'treatment-1', weight: 0.5, isControl: false },
  ],
  'test-002': [
    { variantId: 'test-002-v0', variantName: 'control', weight: 0.4, isControl: true },
    { variantId: 'test-002-v1', variantName: 'treatment-1', weight: 0.3, isControl: false },
    { variantId: 'test-002-v2', variantName: 'treatment-2', weight: 0.3, isControl: false },
  ],
};

const mockResults: Record<string, VariantResult[]> = {
  'test-001': [
    { variantId: 'test-001-v0', variantName: 'control', isControl: true, impressions: 1520, conversions: 486, conversionRate: 0.320, averageLatencyMs: 1850, errorRate: 0.02, revenue: 24300, confidenceInterval: 2.3 },
    { variantId: 'test-001-v1', variantName: 'treatment-1', isControl: false, impressions: 1480, conversions: 532, conversionRate: 0.359, averageLatencyMs: 1920, errorRate: 0.015, revenue: 28600, confidenceInterval: 2.5 },
  ],
};

const statusColor: Record<TestStatus, string> = {
  DRAFT: 'default',
  RUNNING: 'processing',
  PAUSED: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'error',
};

export default function ABTestPage() {
  const [tests, setTests] = useState<ABTest[]>(mockTests);
  const [selectedTest, setSelectedTest] = useState<ABTest | null>(null);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [createFormRef] = Form.useForm();
  const [_variants, _setVariants] = useState<Record<string, ABVariant[]>>(mockVariants);
  const [results] = useState<Record<string, VariantResult[]>>(mockResults);

  const handleCreate = async (values: any) => {
    const test: ABTest = {
      testId: `test-${Date.now()}`,
      name: values.name,
      description: values.description,
      status: TestStatus.DRAFT,
      variantCount: values.variantCount,
      variantWeights: values.weights || Array(values.variantCount).fill(1 / values.variantCount),
      createdTime: Date.now(),
    };
    setTests((prev) => [...prev, test]);
    setCreateModalOpen(false);
    message.success('A/B 测试已创建');
  };

  const handleStart = (testId: string) => {
    setTests((prev) =>
      prev.map((t) => (t.testId === testId ? { ...t, status: TestStatus.RUNNING, startTime: Date.now() } : t))
    );
    message.success('测试已启动');
  };

  const handlePause = (testId: string) => {
    setTests((prev) => prev.map((t) => (t.testId === testId ? { ...t, status: TestStatus.PAUSED } : t)));
    message.info('测试已暂停');
  };

  const handleEnd = (testId: string) => {
    setTests((prev) =>
      prev.map((t) =>
        t.testId === testId ? { ...t, status: TestStatus.COMPLETED, endTime: Date.now() } : t
      )
    );
    message.success('测试已结束');
  };

  const columns: ColumnsType<ABTest> = [
    {
      title: '测试名称',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => (
        <div>
          <Text strong>{text}</Text>
          <br />
          <Text type="secondary" className="text-xs">{record.description}</Text>
        </div>
      ),
    },
    {
      title: '变体数',
      dataIndex: 'variantCount',
      key: 'variantCount',
      width: 80,
      align: 'center',
      render: (n) => <Tag color="blue">{n} 个变体</Tag>,
    },
    {
      title: '流量权重',
      dataIndex: 'variantWeights',
      key: 'weights',
      width: 140,
      render: (weights: number[]) => (
        <Space size={2} wrap>
          {weights.map((w, i) => (
            <Tooltip key={i} title={`变体 ${i} (${i === 0 ? 'control' : `treatment-${i}`})`}>
              <Progress
                percent={Math.round(w * 100)}
                size="small"
                strokeColor={i === 0 ? '#1677ff' : `hsl(${210 + i * 30}, 80%, 50%)`}
                format={() => `${Math.round(w * 100)}%`}
                className="w-24"
              />
            </Tooltip>
          ))}
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: TestStatus) => (
        <Badge status={statusColor[status] as any} text={<span className="text-xs">{status}</span>} />
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
      width: 180,
      render: (_, record) => (
        <Space size="small">
          {record.status === TestStatus.DRAFT && (
            <Button size="small" type="primary" icon={<PlayCircleOutlined />} onClick={() => handleStart(record.testId)}>
              启动
            </Button>
          )}
          {record.status === TestStatus.RUNNING && (
            <>
              <Button size="small" icon={<PauseCircleOutlined />} onClick={() => handlePause(record.testId)}>
                暂停
              </Button>
              <Button size="small" danger icon={<StopOutlined />} onClick={() => handleEnd(record.testId)}>
                结束
              </Button>
            </>
          )}
          {(record.status === TestStatus.COMPLETED || record.status === TestStatus.RUNNING) && (
            <Button size="small" icon={<BarChartOutlined />} onClick={() => setSelectedTest(record)}>
              分析
            </Button>
          )}
          {record.status === TestStatus.DRAFT && (
            <Button size="small" danger icon={<DeleteOutlined />} onClick={() => {
              setTests(prev => prev.filter(t => t.testId !== record.testId));
              message.success('已删除');
            }} />
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Title level={4} className="m-0">A/B 测试管理</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalOpen(true)}>
          创建测试
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={tests}
        rowKey="testId"
        size="small"
        pagination={{ pageSize: 10 }}
      />

      {/* Result detail modal */}
      <Modal
        title="测试结果分析"
        open={!!selectedTest}
        onCancel={() => setSelectedTest(null)}
        footer={null}
        width={800}
      >
        {selectedTest && (
          <div className="space-y-4">
            <Card size="small" title="测试概览">
              <Row gutter={16}>
                <Col span={8}>
                  <Statistic title="测试名称" value={selectedTest.name} />
                </Col>
                <Col span={8}>
                  <Statistic title="变体数" value={selectedTest.variantCount} />
                </Col>
                <Col span={8}>
                  <Statistic
                    title="状态"
                    value={selectedTest.status}
                    valueStyle={{ fontSize: '14px' }}
                  />
                </Col>
              </Row>
            </Card>

            <Card size="small" title="各变体表现">
              <Table
                size="small"
                dataSource={results[selectedTest.testId] || []}
                rowKey="variantId"
                pagination={false}
                columns={[
                  {
                    title: '变体',
                    dataIndex: 'variantName',
                    render: (name, record) => (
                      <Space>
                        <Text strong>{name}</Text>
                        {record.isControl && <Tag>Control</Tag>}
                        {selectedTest.winnerVariantId === record.variantId && (
                          <Tag icon={<CheckCircleFilled />} color="success">Winner</Tag>
                        )}
                      </Space>
                    ),
                  },
                  { title: '曝光量', dataIndex: 'impressions', align: 'right' },
                  { title: '转化数', dataIndex: 'conversions', align: 'right' },
                  {
                    title: '转化率',
                    dataIndex: 'conversionRate',
                    align: 'right',
                    render: (r) => <Text type="success" strong>{(r * 100).toFixed(1)}%</Text>,
                  },
                  {
                    title: '平均延迟',
                    dataIndex: 'averageLatencyMs',
                    align: 'right',
                    render: (ms) => `${ms}ms`,
                  },
                  {
                    title: '错误率',
                    dataIndex: 'errorRate',
                    align: 'right',
                    render: (r) => <Text type="danger">{(r * 100).toFixed(1)}%</Text>,
                  },
                ]}
              />
            </Card>

            <Card size="small" title="统计显著性">
              <Row gutter={16}>
                <Col span={8}>
                  <Statistic
                    title="显著性"
                    value={0.39}
                    suffix="< 0.95"
                    valueStyle={{ color: '#faad14' }}
                  />
                </Col>
                <Col span={8}>
                  <Statistic title="胜出变体" value={results[selectedTest.testId]?.[1]?.variantName || '—'} />
                </Col>
                <Col span={8}>
                  <Statistic
                    title="相对提升"
                    value={`+${(((results[selectedTest.testId]?.[1]?.conversionRate || 0) - (results[selectedTest.testId]?.[0]?.conversionRate || 0)) / (results[selectedTest.testId]?.[0]?.conversionRate || 1) * 100).toFixed(1)}%`}
                    valueStyle={{ color: '#52c41a' }}
                  />
                </Col>
              </Row>
              <Text type="secondary" className="text-xs block mt-2">
                当前曝光量暂未达到最小样本要求 (需要 1000，当前约 1500)，需继续收集数据。
              </Text>
            </Card>
          </div>
        )}
      </Modal>

      {/* Create modal */}
      <Modal
        title="创建 A/B 测试"
        open={createModalOpen}
        onCancel={() => setCreateModalOpen(false)}
        onOk={() => createFormRef.submit()}
      >
        <Form
          form={createFormRef}
          layout="vertical"
          onFinish={handleCreate}
          initialValues={{ variantCount: 2, name: '', description: '' }}
        >
          <Form.Item name="name" label="测试名称" rules={[{ required: true }]}>
            <Input placeholder="如: RAG 检索策略对比测试" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="variantCount" label="变体数量" rules={[{ required: true }]}>
            <Slider min={2} max={5} marks={{ 2: '2', 3: '3', 4: '4', 5: '5' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
