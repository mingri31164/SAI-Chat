import { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Typography,
  Modal,
  Form,
  Input,
  Select,
  Slider,
  Progress,
  message,
  Badge,
  Row,
  Col,
} from 'antd';
import {
  PlusOutlined,
  RocketOutlined,
  CheckCircleOutlined,
  UndoOutlined,
  PlayCircleOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { ExperimentStatus } from '@/types/document';
import type { OptimizationExperiment, OptimizationSuggestion } from '@/types/document';

const { Title, Text } = Typography;

const mockSuggestions: OptimizationSuggestion[] = [
  {
    type: 'latency',
    description: 'P95 延迟偏高，建议优化检索策略',
    targetMetric: 'p95_latency',
    currentValue: 4800,
    targetValue: 3000,
    confidence: 0.85,
  },
  {
    type: 'accuracy',
    description: '意图分类准确率偏低，建议调整意图树',
    targetMetric: 'intent_accuracy',
    currentValue: 0.72,
    targetValue: 0.85,
    confidence: 0.78,
  },
  {
    type: 'cost',
    description: 'Token 消耗过高，建议精简 Prompt',
    targetMetric: 'token_cost',
    currentValue: 3800,
    targetValue: 2500,
    confidence: 0.92,
  },
];

const mockExperiments: OptimizationExperiment[] = [
  {
    experimentId: 'exp-001',
    suggestion: mockSuggestions[0],
    status: ExperimentStatus.RUNNING,
    baselineMetrics: { p95_latency: 4800 },
    currentMetrics: { p95_latency: 4200 },
    improvements: { p95_latency: -12.5 },
    rolloutPercentage: 50,
    createdAt: Date.now() - 86400000,
    startedAt: Date.now() - 43200000,
  },
  {
    experimentId: 'exp-002',
    suggestion: mockSuggestions[2],
    status: ExperimentStatus.COMPLETED,
    baselineMetrics: { token_cost: 3800 },
    currentMetrics: { token_cost: 2400 },
    improvements: { token_cost: -36.8 },
    rolloutPercentage: 100,
    createdAt: Date.now() - 86400000 * 3,
    startedAt: Date.now() - 86400000 * 2,
    completedAt: Date.now() - 86400000,
  },
];

const statusColor: Record<ExperimentStatus, string> = {
  PENDING: 'default',
  RUNNING: 'processing',
  COMPLETED: 'success',
  FAILED: 'error',
  ROLLED_BACK: 'warning',
};

export default function ExperimentPage() {
  const [experiments, setExperiments] = useState<OptimizationExperiment[]>(mockExperiments);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [selectedSuggestion, setSelectedSuggestion] = useState<OptimizationSuggestion | null>(null);
  const [rolloutModal, setRolloutModal] = useState<{ id: string; percentage: number } | null>(null);
  const [form] = Form.useForm();

  const handleCreate = (_values: any) => {
    const exp: OptimizationExperiment = {
      experimentId: `exp-${Date.now()}`,
      suggestion: selectedSuggestion!,
      status: ExperimentStatus.PENDING,
      rolloutPercentage: 0,
      createdAt: Date.now(),
    };
    setExperiments((prev) => [...prev, exp]);
    setCreateModalOpen(false);
    message.success('实验已创建');
  };

  const handleStart = (id: string) => {
    setExperiments((prev) =>
      prev.map((e) =>
        e.experimentId === id
          ? { ...e, status: ExperimentStatus.RUNNING, startedAt: Date.now() }
          : e
      )
    );
    message.success('实验已启动');
  };

  const handleRollout = (id: string, percentage: number) => {
    setExperiments((prev) =>
      prev.map((e) =>
        e.experimentId === id ? { ...e, rolloutPercentage: percentage } : e
      )
    );
    message.success(`灰度发布已调整为 ${percentage}%`);
    setRolloutModal(null);
  };

  const columns: ColumnsType<OptimizationExperiment> = [
    {
      title: '实验 ID',
      dataIndex: 'experimentId',
      key: 'experimentId',
      render: (id) => <Text code className="text-xs">{id}</Text>,
    },
    {
      title: '优化目标',
      dataIndex: ['suggestion', 'description'],
      key: 'description',
      render: (desc, record) => (
        <Space direction="vertical" size={0}>
          <Text>{desc}</Text>
          <Text type="secondary" className="text-xs">
            {record.suggestion.targetMetric}: {record.suggestion.currentValue} → {record.suggestion.targetValue}
          </Text>
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (s: ExperimentStatus) => (
        <Badge status={statusColor[s] as any} text={<span className="text-xs">{s}</span>} />
      ),
    },
    {
      title: '改进幅度',
      dataIndex: 'improvements',
      key: 'improvements',
      width: 120,
      render: (imp: Record<string, number>) => {
        const val = imp ? Object.values(imp)[0] : 0;
        return (
          <Text type={val > 0 ? 'success' : 'danger'} strong>
            {val > 0 ? '+' : ''}{val.toFixed(1)}%
          </Text>
        );
      },
    },
    {
      title: '灰度发布',
      dataIndex: 'rolloutPercentage',
      key: 'rollout',
      width: 140,
      render: (pct: number, _record) => (
        <div className="w-28">
          <Progress
            percent={pct}
            size="small"
            strokeColor={pct === 100 ? '#52c41a' : pct > 0 ? '#1677ff' : '#d9d9d9'}
            format={() => ''}
          />
          <Text type="secondary" className="text-xs">{pct}%</Text>
        </div>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      render: (_, record) => (
        <Space size="small">
          {record.status === ExperimentStatus.PENDING && (
            <Button size="small" type="primary" icon={<PlayCircleOutlined />} onClick={() => handleStart(record.experimentId)}>
              启动
            </Button>
          )}
          {record.status === ExperimentStatus.RUNNING && (
            <>
              <Button
                size="small"
                icon={<RocketOutlined />}
                onClick={() => setRolloutModal({ id: record.experimentId, percentage: Math.min(100, record.rolloutPercentage + 10) })}
              >
                灰度
              </Button>
              <Button
                size="small"
                danger
                icon={<UndoOutlined />}
                onClick={() => {
                  setExperiments(prev => prev.map(e => e.experimentId === record.experimentId ? { ...e, status: ExperimentStatus.ROLLED_BACK } : e));
                  message.info('已回滚');
                }}
              >
                回滚
              </Button>
            </>
          )}
          {record.status === ExperimentStatus.COMPLETED && (
            <Button size="small" icon={<CheckCircleOutlined />} onClick={() => {
              setExperiments(prev => prev.map(e => e.experimentId === record.experimentId ? { ...e, status: ExperimentStatus.ROLLED_BACK } : e));
              message.info('已回滚到基线');
            }}>
              回滚
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Title level={4} className="m-0">实验管理</Title>
        <Space>
          <Button onClick={() => message.info('分析中...')}>运行性能分析</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalOpen(true)}>
            创建实验
          </Button>
        </Space>
      </div>

      {/* Suggestions */}
      <Card size="small" title="优化建议">
        <Row gutter={[16, 16]}>
          {mockSuggestions.map((s, i) => (
            <Col key={i} xs={24} sm={8}>
              <Card
                size="small"
                className="cursor-pointer card-hover"
                onClick={() => { setSelectedSuggestion(s); setCreateModalOpen(true); }}
                bodyStyle={{ padding: 12 }}
              >
                <div className="flex justify-between items-start mb-2">
                  <Tag color={s.type === 'latency' ? 'blue' : s.type === 'cost' ? 'green' : 'orange'}>
                    {s.type}
                  </Tag>
                  <Text type="secondary" className="text-xs">
                    置信度 {(s.confidence * 100).toFixed(0)}%
                  </Text>
                </div>
                <Text className="text-sm">{s.description}</Text>
                <div className="mt-2 flex gap-2">
                  <Text type="secondary" className="text-xs">
                    当前: {s.currentValue}
                  </Text>
                  <Text type="secondary" className="text-xs">→</Text>
                  <Text type="success" className="text-xs">
                    目标: {s.targetValue}
                  </Text>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      </Card>

      {/* Experiments */}
      <Table
        columns={columns}
        dataSource={experiments}
        rowKey="experimentId"
        size="small"
        pagination={{ pageSize: 10 }}
      />

      {/* Rollout modal */}
      <Modal
        title="灰度发布"
        open={!!rolloutModal}
        onCancel={() => setRolloutModal(null)}
        onOk={() => rolloutModal && handleRollout(rolloutModal.id, rolloutModal.percentage)}
      >
        {rolloutModal && (
          <div className="space-y-4">
            <Text>选择灰度流量比例：</Text>
            <Slider
              min={0}
              max={100}
              step={10}
              value={rolloutModal.percentage}
              onChange={(v) => setRolloutModal({ id: rolloutModal.id, percentage: v })}
              marks={{ 0: '0%', 25: '25%', 50: '50%', 75: '75%', 100: '100%' }}
            />
            <Text type="secondary" className="text-xs">
              当前: {rolloutModal.percentage}% 流量将使用新实验版本
            </Text>
          </div>
        )}
      </Modal>

      {/* Create modal */}
      <Modal
        title="创建优化实验"
        open={createModalOpen}
        onOk={() => form.submit()}
        onCancel={() => { setCreateModalOpen(false); setSelectedSuggestion(null); }}
      >
        {selectedSuggestion ? (
          <div className="space-y-3">
            <div className="bg-blue-50 rounded p-3">
              <Text strong>基于建议创建实验</Text>
              <br />
              <Text type="secondary" className="text-xs">{selectedSuggestion.description}</Text>
            </div>
            <Text type="secondary" className="text-xs">
              确认后系统将基于此建议创建优化实验，包括基线记录、灰度发布和结果评估。
            </Text>
          </div>
        ) : (
          <Form form={form} layout="vertical" onFinish={handleCreate}>
            <Form.Item name="description" label="实验描述" rules={[{ required: true }]}>
              <Input.TextArea rows={3} placeholder="描述实验目的和预期改进..." />
            </Form.Item>
            <Form.Item name="targetMetric" label="目标指标" rules={[{ required: true }]}>
              <Select
                options={[
                  { label: 'P95 延迟 (ms)', value: 'p95_latency' },
                  { label: '意图准确率', value: 'intent_accuracy' },
                  { label: 'Token 消耗', value: 'token_cost' },
                  { label: '成功率', value: 'success_rate' },
                ]}
              />
            </Form.Item>
          </Form>
        )}
      </Modal>
    </div>
  );
}
