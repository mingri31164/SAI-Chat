import { useState } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Typography,
  Space,
  Table,
  Tag,
  Badge,
  Button,
  Tabs,
  List,
  Progress,
  Spin,
  message,
} from 'antd';
import {
  LineChartOutlined,
  BarChartOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ThunderboltOutlined,
  RobotOutlined,
  ExclamationCircleOutlined,
  RocketOutlined,
} from '@ant-design/icons';
import { useMetrics } from '@/hooks/useMetrics';
import { useObserveStore } from '@/stores/observeStore';
import { formatDuration, formatPercent, getAgentStatusColor } from '@/utils/format';
import type { AgentTrace, Span } from '@/types/observe';

const { Title, Text } = Typography;

export default function ObservabilityPage() {
  const { metricsSnapshot, recentTraces } = useMetrics();
  const { isLoading } = useObserveStore();
  const [selectedTrace, setSelectedTrace] = useState<AgentTrace | null>(null);

  const spanColumns = [
    {
      title: 'Span 名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: Span) => (
        <Space>
          <Text code className="text-xs">{record.spanId.slice(-8)}</Text>
          <Text>{name}</Text>
          <Tag className="text-xs">{record.type}</Tag>
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (s: string) => (
        <Badge
          color={
            s === 'SUCCESS' ? '#52c41a' :
            s === 'FAILED' ? '#ff4d4f' :
            s === 'RUNNING' ? '#1677ff' : '#8c8c8c'
          }
          text={<span className="text-xs">{s}</span>}
        />
      ),
    },
    {
      title: '耗时',
      dataIndex: 'durationMs',
      key: 'durationMs',
      width: 100,
      align: 'right' as const,
      render: (ms: number) => (
        <Text className="text-xs">{ms ? formatDuration(ms) : '—'}</Text>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 160,
      render: (t: string) => <Tag className="text-xs">{t}</Tag>,
    },
  ];

  return (
    <div className="space-y-4">
      <Title level={4} className="m-0">可观测性</Title>

      {/* Metrics summary */}
      {isLoading && !metricsSnapshot ? (
        <div className="flex items-center justify-center h-32">
          <Spin />
        </div>
      ) : metricsSnapshot ? (
        <Row gutter={[16, 16]}>
          <Col xs={12} sm={6}>
            <Card size="small"><Statistic title="成功率" value={formatPercent(metricsSnapshot.successRate)} prefix={<CheckCircleOutlined />} valueStyle={{ color: '#52c41a' }} /></Card>
          </Col>
          <Col xs={12} sm={6}>
            <Card size="small"><Statistic title="QPS" value={metricsSnapshot.qps.toFixed(2)} prefix={<ThunderboltOutlined />} valueStyle={{ color: '#3b82f6' }} /></Card>
          </Col>
          <Col xs={12} sm={6}>
            <Card size="small"><Statistic title="P95 延迟" value={formatDuration(metricsSnapshot.p95LatencyMs)} prefix={<ClockCircleOutlined />} /></Card>
          </Col>
          <Col xs={12} sm={6}>
            <Card size="small"><Statistic title="Token 消耗" value={metricsSnapshot.totalTokens.toLocaleString()} prefix={<RobotOutlined />} valueStyle={{ color: '#8b5cf6' }} /></Card>
          </Col>
        </Row>
      ) : (
        <Card><div className="text-center py-8"><ExclamationCircleOutlined style={{ fontSize: 32, color: '#ff4d4f' }} /><Text type="secondary" className="block mt-2">后端服务未连接，使用 Mock 数据展示</Text></div></Card>
      )}

      <Tabs
        items={[
          {
            key: 'traces',
            label: <span><BarChartOutlined /> 调用链路</span>,
            children: (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                {/* Trace list */}
                <Card size="small" title="最近调用链路" extra={<Badge status="processing" text="实时" />}>
                  <Table
                    size="small"
                    dataSource={recentTraces.slice(0, 10)}
                    rowKey="traceId"
                    pagination={false}
                    onRow={(record) => ({
                      onClick: () => setSelectedTrace(record),
                      style: { cursor: 'pointer' },
                      className: selectedTrace?.traceId === record.traceId ? 'bg-blue-50' : '',
                    })}
                    columns={[
                      {
                        title: 'Trace ID',
                        dataIndex: 'traceId',
                        key: 'traceId',
                        render: (id) => <Text code className="text-xs">{id.slice(-12)}</Text>,
                      },
                      {
                        title: '状态',
                        dataIndex: 'status',
                        key: 'status',
                        render: (s) => (
                          <Badge color={getAgentStatusColor(s)} text={<span className="text-xs">{s}</span>} />
                        ),
                      },
                      {
                        title: '耗时',
                        dataIndex: 'totalDurationMs',
                        key: 'duration',
                        render: (ms: number) => <Text className="text-xs">{formatDuration(ms || 0)}</Text>,
                      },
                      {
                        title: 'Spans',
                        dataIndex: 'spanList',
                        key: 'spans',
                        render: (spans: Span[]) => <Tag>{spans.length}</Tag>,
                      },
                    ]}
                  />
                </Card>

                {/* Trace detail */}
                <Card
                  size="small"
                  title={
                    <Space>
                      <span>链路详情</span>
                      {selectedTrace && (
                        <Text code className="text-xs">{selectedTrace.traceId.slice(-12)}</Text>
                      )}
                    </Space>
                  }
                >
                  {selectedTrace ? (
                    <div className="space-y-3">
                      <div className="grid grid-cols-3 gap-2">
                        <div className="bg-gray-50 rounded p-2">
                          <Text type="secondary" className="text-xs">状态</Text>
                          <br />
                          <Badge color={getAgentStatusColor(selectedTrace.status)} text={selectedTrace.status} />
                        </div>
                        <div className="bg-gray-50 rounded p-2">
                          <Text type="secondary" className="text-xs">会话</Text>
                          <br />
                          <Text className="text-xs">{selectedTrace.sessionId.slice(-8)}</Text>
                        </div>
                        <div className="bg-gray-50 rounded p-2">
                          <Text type="secondary" className="text-xs">耗时</Text>
                          <br />
                          <Text className="text-xs">{formatDuration(selectedTrace.totalDurationMs || 0)}</Text>
                        </div>
                      </div>

                      {selectedTrace.errorMessage && (
                        <div className="bg-red-50 border border-red-200 rounded p-2">
                          <Text type="danger" className="text-xs">{selectedTrace.errorMessage}</Text>
                        </div>
                      )}

                      <Text strong className="text-xs">Span 列表</Text>
                      <Table
                        size="small"
                        dataSource={selectedTrace.spanList}
                        rowKey="spanId"
                        pagination={false}
                        columns={spanColumns}
                      />
                    </div>
                  ) : (
                    <div className="text-center py-8">
                      <Text type="secondary">点击左侧链路查看详情</Text>
                    </div>
                  )}
                </Card>
              </div>
            ),
          },
          {
            key: 'eval',
            label: <span><RocketOutlined /> 评测中心</span>,
            children: (
              <Card size="small" title="评测用例库">
                <Tabs
                  items={[
                    {
                      key: 'simple-qa',
                      label: '简单问答 (3)',
                      children: (
                        <List
                          size="small"
                          dataSource={[
                            { id: 'qa-001', name: '问候测试', difficulty: 'EASY', tags: ['基础'] },
                            { id: 'qa-002', name: '自我介绍', difficulty: 'EASY', tags: ['基础'] },
                            { id: 'qa-003', name: '时间查询', difficulty: 'MEDIUM', tags: ['基础'] },
                          ]}
                          renderItem={(item) => (
                            <List.Item>
                              <List.Item.Meta
                                title={<Text className="text-sm">{item.name}</Text>}
                                description={<Space><Tag>{item.difficulty}</Tag>{item.tags.map(t => <Tag key={t}>{t}</Tag>)}</Space>}
                              />
                            </List.Item>
                          )}
                        />
                      ),
                    },
                    {
                      key: 'tool-call',
                      label: '工具调用 (2)',
                      children: (
                        <List
                          size="small"
                          dataSource={[
                            { id: 'tool-001', name: '天气查询', difficulty: 'MEDIUM', tags: ['MCP'] },
                            { id: 'tool-002', name: '数据库查询', difficulty: 'HARD', tags: ['MCP'] },
                          ]}
                          renderItem={(item) => (
                            <List.Item>
                              <List.Item.Meta
                                title={<Text className="text-sm">{item.name}</Text>}
                                description={<Space><Tag>{item.difficulty}</Tag>{item.tags.map(t => <Tag key={t}>{t}</Tag>)}</Space>}
                              />
                            </List.Item>
                          )}
                        />
                      ),
                    },
                    {
                      key: 'reasoning',
                      label: '推理 (3)',
                      children: (
                        <List
                          size="small"
                          dataSource={[
                            { id: 'reason-001', name: '数学计算', difficulty: 'MEDIUM', tags: ['推理'] },
                            { id: 'reason-002', name: '逻辑推理', difficulty: 'HARD', tags: ['推理'] },
                            { id: 'reason-003', name: '多步推理', difficulty: 'EXPERT', tags: ['推理'] },
                          ]}
                          renderItem={(item) => (
                            <List.Item>
                              <List.Item.Meta
                                title={<Text className="text-sm">{item.name}</Text>}
                                description={<Space><Tag color={item.difficulty === 'EXPERT' ? 'red' : item.difficulty === 'HARD' ? 'orange' : 'blue'}>{item.difficulty}</Tag>{item.tags.map(t => <Tag key={t}>{t}</Tag>)}</Space>}
                              />
                            </List.Item>
                          )}
                        />
                      ),
                    },
                  ]}
                />
                <div className="mt-4 flex gap-2">
                  <Button type="primary" icon={<RocketOutlined />} onClick={() => message.info('评测启动中...')}>
                    运行全部评测
                  </Button>
                  <Button icon={<RocketOutlined />} onClick={() => message.info('快速测试中...')}>
                    快速冒烟测试
                  </Button>
                </div>
              </Card>
            ),
          },
          {
            key: 'metrics-detail',
            label: <span><LineChartOutlined /> 指标详情</span>,
            children: (
              <Row gutter={[16, 16]}>
                <Col span={24}>
                  <Card size="small" title="性能分布">
                    <div className="space-y-3">
                      <div className="flex items-center gap-2">
                        <Text className="text-xs w-16">P50</Text>
                        <Progress
                          percent={metricsSnapshot ? Math.min(100, (metricsSnapshot.p50LatencyMs / 5000) * 100) : 0}
                          size="small"
                          strokeColor="#52c41a"
                          format={() => metricsSnapshot ? formatDuration(metricsSnapshot.p50LatencyMs) : '—'}
                        />
                      </div>
                      <div className="flex items-center gap-2">
                        <Text className="text-xs w-16">P95</Text>
                        <Progress
                          percent={metricsSnapshot ? Math.min(100, (metricsSnapshot.p95LatencyMs / 10000) * 100) : 0}
                          size="small"
                          strokeColor="#faad14"
                          format={() => metricsSnapshot ? formatDuration(metricsSnapshot.p95LatencyMs) : '—'}
                        />
                      </div>
                      <div className="flex items-center gap-2">
                        <Text className="text-xs w-16">P99</Text>
                        <Progress
                          percent={metricsSnapshot ? Math.min(100, (metricsSnapshot.p99LatencyMs / 15000) * 100) : 0}
                          size="small"
                          strokeColor="#ff4d4f"
                          format={() => metricsSnapshot ? formatDuration(metricsSnapshot.p99LatencyMs) : '—'}
                        />
                      </div>
                    </div>
                  </Card>
                </Col>
              </Row>
            ),
          },
        ]}
      />
    </div>
  );
}
