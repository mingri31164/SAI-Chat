import { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Typography,
  List,
  Rate,
  message,
  Tabs,
  Badge,
  Progress,
  Modal,
  Form,
  Input,
} from 'antd';
import {
  PlusOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  StarOutlined,
  WarningOutlined,
  ExperimentOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import type { SuccessPattern, FailureLesson, ReviewReport } from '@/types/document';

const { Title, Text } = Typography;
const { TextArea } = Input;

const mockPatterns: SuccessPattern[] = [
  {
    patternId: 'sp-001',
    title: '多轮对话指代消解',
    pattern: '用户多次提问时，前代词需要关联前文提到的实体',
    context: '多轮对话、追问场景',
    triggerCondition: '问题包含"这个"、"它"、"上面"等代词',
    steps: ['提取上文中提到的实体', '将代词替换为具体实体', '验证替换正确性'],
    effectivenessScore: 92,
    usageCount: 156,
    tags: ['多轮', '指代消解'],
    accessCount: 42,
    createdAt: Date.now() - 86400000 * 5,
  },
  {
    patternId: 'sp-002',
    title: 'MCP 工具参数提取',
    pattern: 'LLM 提取用户问题中的参数值',
    context: 'MCP 工具调用',
    triggerCondition: '用户问题涉及销售/天气/工单等实时数据查询',
    steps: ['识别工具类型', '从问题中提取参数', '调用 MCP 工具', '将结果转为自然语言'],
    effectivenessScore: 88,
    usageCount: 89,
    tags: ['MCP', '参数提取'],
    accessCount: 31,
    createdAt: Date.now() - 86400000 * 3,
  },
  {
    patternId: 'sp-003',
    title: 'RAG 检索去重合并',
    pattern: '多路检索结果按 ID 去重，取最高分',
    context: 'RAG 多通道检索',
    triggerCondition: '多路检索返回重叠文档',
    steps: ['按文档 ID 分组', '保留最高分结果', '合并输出'],
    effectivenessScore: 85,
    usageCount: 234,
    tags: ['RAG', '去重'],
    accessCount: 67,
    createdAt: Date.now() - 86400000 * 7,
  },
];

const mockLessons: FailureLesson[] = [
  {
    lessonId: 'fl-001',
    title: '意图分类边界模糊',
    lesson: '意图树中相邻节点的分类标准不够清晰，导致分类结果不稳定',
    rootCause: '节点描述过于笼统，缺乏区分性描述',
    prevention: '为每个节点编写明确的区分性描述和示例',
    symptoms: ['同一问题多次分类结果不一致', '分类置信度在 0.5 附近徘徊'],
    occurrenceCount: 12,
    tags: ['意图识别', '分类'],
    createdAt: Date.now() - 86400000 * 2,
  },
  {
    lessonId: 'fl-002',
    title: '长对话 Token 爆炸',
    lesson: '会话历史过长导致 Token 超出模型限制，回答质量下降',
    rootCause: '滑动窗口阈值设置过大，且摘要触发条件不合理',
    prevention: '设置合理的最大历史长度，及时触发摘要压缩',
    symptoms: ['回答开始重复', 'Token 计数异常高', '延迟明显增加'],
    occurrenceCount: 8,
    tags: ['记忆', 'Token'],
    createdAt: Date.now() - 86400000 * 4,
  },
];

const mockReport: ReviewReport = {
  traceId: 'trace-demo',
  sessionId: 'session-demo',
  analysisTime: Date.now(),
  successFactors: ['意图分类准确', '检索结果相关度高', '回答简洁有条理'],
  failureReasons: ['长问题分块不完整', '部分专业术语未识别'],
  keyDecisions: ['选择知识库检索而非 MCP 工具', '拆分为 2 个子问题'],
  overallScore: 78,
  recommendations: ['优化问题分块策略', '增强术语识别能力'],
};

export default function ReviewPage() {
  const [patterns, setPatterns] = useState<SuccessPattern[]>(mockPatterns);
  const [_lessons, _setLessons] = useState<FailureLesson[]>(mockLessons);
  const [addPatternModal, setAddPatternModal] = useState(false);
  const [addLessonModal, setAddLessonModal] = useState(false);
  const [form] = Form.useForm();

  const patternColumns: ColumnsType<SuccessPattern> = [
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{text}</Text>
          <Text type="secondary" className="text-xs">{record.context}</Text>
        </Space>
      ),
    },
    {
      title: '有效性',
      dataIndex: 'effectivenessScore',
      key: 'effectivenessScore',
      width: 120,
      render: (score) => (
        <Space>
          <Rate disabled value={Math.round(score / 20)} allowHalf className="text-xs" />
          <Text className="text-xs">{score}%</Text>
        </Space>
      ),
    },
    {
      title: '使用次数',
      dataIndex: 'usageCount',
      key: 'usageCount',
      width: 90,
      align: 'center',
      render: (n) => <Badge count={n} style={{ backgroundColor: '#3b82f6' }} />,
    },
    {
      title: '标签',
      dataIndex: 'tags',
      key: 'tags',
      render: (tags: string[]) => (
        <Space size={2} wrap>
          {tags.map((t) => <Tag key={t}>{t}</Tag>)}
        </Space>
      ),
    },
    {
      title: '触发条件',
      dataIndex: 'triggerCondition',
      key: 'triggerCondition',
      render: (text) => <Text type="secondary" className="text-xs">{text}</Text>,
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Button size="small" danger type="link" onClick={() => {
          setPatterns(prev => prev.filter(p => p.patternId !== record.patternId));
          message.success('已删除');
        }}>
          删除
        </Button>
      ),
    },
  ];

  const lessonColumns: ColumnsType<FailureLesson> = [
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (
        <Space direction="vertical" size={0}>
          <Space>
            <Text strong>{text}</Text>
            <Tag color="red">{record.occurrenceCount} 次</Tag>
          </Space>
          <Text type="secondary" className="text-xs">{record.rootCause}</Text>
        </Space>
      ),
    },
    {
      title: '教训',
      dataIndex: 'lesson',
      key: 'lesson',
      render: (text) => <Text className="text-xs">{text}</Text>,
    },
    {
      title: '预防措施',
      dataIndex: 'prevention',
      key: 'prevention',
      render: (text) => <Text type="secondary" className="text-xs">{text}</Text>,
    },
    {
      title: '症状',
      dataIndex: 'symptoms',
      key: 'symptoms',
      render: (symptoms: string[]) => (
        <Space size={2} wrap>
          {symptoms.map((s) => <Tag key={s} className="text-xs">{s}</Tag>)}
        </Space>
      ),
    },
    {
      title: '标签',
      dataIndex: 'tags',
      key: 'tags',
      render: (tags: string[]) => tags.map(t => <Tag key={t}>{t}</Tag>),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Title level={4} className="m-0">复盘分析</Title>
        <Space>
          <Button icon={<PlusOutlined />} onClick={() => setAddPatternModal(true)}>添加成功模式</Button>
          <Button icon={<PlusOutlined />} onClick={() => setAddLessonModal(true)}>添加失败教训</Button>
        </Space>
      </div>

      <Tabs
        items={[
          {
            key: 'patterns',
            label: <span><CheckCircleOutlined /> 成功模式库</span>,
            children: (
              <Table
                columns={patternColumns}
                dataSource={patterns}
                rowKey="patternId"
                size="small"
                pagination={{ pageSize: 10 }}
              />
            ),
          },
          {
            key: 'lessons',
            label: <span><CloseCircleOutlined /> 失败教训库</span>,
            children: (
              <Table
                columns={lessonColumns}
                dataSource={_lessons}
                rowKey="lessonId"
                size="small"
                pagination={{ pageSize: 10 }}
              />
            ),
          },
          {
            key: 'analysis',
            label: <span><ExperimentOutlined /> 单次复盘</span>,
            children: (
              <Card size="small" title={`复盘报告 - ${mockReport.traceId.slice(-12)}`}>
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                  <div>
                    <Text strong className="text-xs">综合评分</Text>
                    <div className="mt-1">
                      <Progress
                        percent={mockReport.overallScore}
                        strokeColor={mockReport.overallScore >= 80 ? '#52c41a' : mockReport.overallScore >= 60 ? '#faad14' : '#ff4d4f'}
                        format={(p) => <Text strong>{p}</Text>}
                      />
                    </div>
                  </div>

                  <div>
                    <Text strong className="text-xs">成功因素</Text>
                    <List
                      size="small"
                      dataSource={mockReport.successFactors}
                      renderItem={(item) => (
                        <List.Item style={{ padding: '4px 0' }}>
                          <CheckCircleOutlined style={{ color: '#52c41a', marginRight: 8 }} />
                          <Text className="text-sm">{item}</Text>
                        </List.Item>
                      )}
                    />
                  </div>

                  <div>
                    <Text strong className="text-xs">失败原因</Text>
                    <List
                      size="small"
                      dataSource={mockReport.failureReasons}
                      renderItem={(item) => (
                        <List.Item style={{ padding: '4px 0' }}>
                          <CloseCircleOutlined style={{ color: '#ff4d4f', marginRight: 8 }} />
                          <Text className="text-sm">{item}</Text>
                        </List.Item>
                      )}
                    />
                  </div>

                  <div>
                    <Text strong className="text-xs">关键决策</Text>
                    <List
                      size="small"
                      dataSource={mockReport.keyDecisions}
                      renderItem={(item) => (
                        <List.Item style={{ padding: '4px 0' }}>
                          <StarOutlined style={{ color: '#faad14', marginRight: 8 }} />
                          <Text className="text-sm">{item}</Text>
                        </List.Item>
                      )}
                    />
                  </div>

                  <div className="lg:col-span-2">
                    <Text strong className="text-xs">建议</Text>
                    <List
                      size="small"
                      dataSource={mockReport.recommendations}
                      renderItem={(item) => (
                        <List.Item style={{ padding: '4px 0' }}>
                          <WarningOutlined style={{ color: '#1677ff', marginRight: 8 }} />
                          <Text className="text-sm">{item}</Text>
                        </List.Item>
                      )}
                    />
                  </div>
                </div>
              </Card>
            ),
          },
        ]}
      />

      {/* Add Pattern Modal */}
      <Modal
        title="添加成功模式"
        open={addPatternModal}
        onOk={() => {
          form.validateFields().then(() => {
            message.success('成功模式已添加');
            setAddPatternModal(false);
            form.resetFields();
          });
        }}
        onCancel={() => setAddPatternModal(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="标题" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="pattern" label="模式描述" rules={[{ required: true }]}>
            <TextArea rows={2} />
          </Form.Item>
          <Form.Item name="context" label="适用场景">
            <Input />
          </Form.Item>
          <Form.Item name="triggerCondition" label="触发条件">
            <TextArea rows={2} />
          </Form.Item>
          <Form.Item name="steps" label="执行步骤">
            <TextArea rows={3} placeholder="每行一个步骤" />
          </Form.Item>
        </Form>
      </Modal>

      {/* Add Lesson Modal */}
      <Modal
        title="添加失败教训"
        open={addLessonModal}
        onOk={() => {
          form.validateFields().then(() => {
            message.success('失败教训已添加');
            setAddLessonModal(false);
            form.resetFields();
          });
        }}
        onCancel={() => setAddLessonModal(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="标题" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="lesson" label="教训内容" rules={[{ required: true }]}>
            <TextArea rows={2} />
          </Form.Item>
          <Form.Item name="rootCause" label="根本原因">
            <TextArea rows={2} />
          </Form.Item>
          <Form.Item name="prevention" label="预防措施">
            <TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
