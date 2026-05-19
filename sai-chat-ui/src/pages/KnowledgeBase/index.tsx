import React, { useState, useCallback } from 'react';
import {
  Card,
  Upload,
  Table,
  Tag,
  Space,
  Typography,
  Progress,
  Button,
  message,
  Tabs,
  List,
  Popconfirm,
  Input,
  Select,
  Alert,
} from 'antd';
import {
  UploadOutlined,
  InboxOutlined,
  DeleteOutlined,
  DatabaseOutlined,
  FileTextOutlined,
  CloudServerOutlined,
  GithubOutlined,
} from '@ant-design/icons';
import type { UploadProps, TableColumnsType } from 'antd';
import { getRagTagList } from '@/api/document';
import type { TagInfo, ChunkMetadata } from '@/types/document';

const { Title, Text } = Typography;
const { Dragger } = Upload;

type UploadStage = 'idle' | 'parsing' | 'chunking' | 'embedding' | 'indexing' | 'complete' | 'error';

interface UploadState {
  stage: UploadStage;
  progress: number;
  message: string;
  ragTag: string;
}

export default function KnowledgeBase() {
  const [tags, setTags] = useState<TagInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [tagInput, setTagInput] = useState('');
  const [, setSelectedTag] = useState<string>('');
  const [uploadState, setUploadState] = useState<UploadState>({
    stage: 'idle',
    progress: 0,
    message: '',
    ragTag: '',
  });
  const [mockChunks] = useState<ChunkMetadata[]>([
    {
      chunkId: 'chunk-001',
      docId: 'doc-001',
      kbId: 'kb-hr',
      docTitle: '员工手册 v2.1',
      content: '第一条：员工入职需提交身份证、学历证明、体检报告等材料...',
      index: 0,
      sectionPath: '第一章 / 入职须知',
      score: 0.95,
    },
    {
      chunkId: 'chunk-002',
      docId: 'doc-001',
      kbId: 'kb-hr',
      docTitle: '员工手册 v2.1',
      content: '第二条：试用期一般为三个月，试用期工资为正式工资的 80%...',
      index: 1,
      sectionPath: '第一章 / 入职须知',
      score: 0.88,
    },
    {
      chunkId: 'chunk-003',
      docId: 'doc-002',
      kbId: 'kb-oa',
      docTitle: 'OA 系统使用指南',
      content: '如何提交请假申请：登录 OA 系统后，点击「请假管理」→「新建请假」...',
      index: 0,
      sectionPath: '第二章 / 请假流程',
      score: 0.92,
    },
  ]);

  const loadTags = useCallback(async () => {
    setLoading(true);
    try {
      const res = await getRagTagList();
      setTags(res.data as unknown as TagInfo[]);
    } catch {
      // Mock data for demo
      setTags([
        { tag: '集团信息化-HR', documentCount: 5, chunkCount: 128 },
        { tag: '业务系统-OA', documentCount: 8, chunkCount: 256 },
        { tag: '业务系统-保险', documentCount: 3, chunkCount: 64 },
      ]);
    } finally {
      setLoading(false);
    }
  }, []);

  React.useEffect(() => {
    loadTags();
  }, [loadTags]);

  const uploadProps: UploadProps = {
    name: 'file',
    multiple: true,
    accept: '.pdf,.docx,.doc,.pptx,.ppt,.xlsx,.xls,.txt,.html,.md',
    showUploadList: false,
    beforeUpload: async (file) => {
      const ragTag = tagInput.trim() || file.name.replace(/\.[^.]+$/, '');
      setUploadState({ stage: 'parsing', progress: 10, message: `正在解析文件: ${file.name}`, ragTag });

      // Simulate upload progress
      await new Promise((r) => setTimeout(r, 800));
      setUploadState({ stage: 'chunking', progress: 30, message: '正在分块...', ragTag });
      await new Promise((r) => setTimeout(r, 600));
      setUploadState({ stage: 'embedding', progress: 60, message: '正在向量化...', ragTag });
      await new Promise((r) => setTimeout(r, 1000));
      setUploadState({ stage: 'indexing', progress: 85, message: '正在入库...', ragTag });
      await new Promise((r) => setTimeout(r, 500));
      setUploadState({ stage: 'complete', progress: 100, message: '上传成功！', ragTag });
      message.success(`${file.name} 上传成功`);
      loadTags();
      return false; // Prevent default upload
    },
  };

  const columns: TableColumnsType<ChunkMetadata> = [
    {
      title: 'Chunk ID',
      dataIndex: 'chunkId',
      key: 'chunkId',
      width: 150,
      render: (text) => <Text code className="text-xs">{text}</Text>,
    },
    {
      title: '文档标题',
      dataIndex: 'docTitle',
      key: 'docTitle',
      width: 180,
      render: (text) => (
        <Space>
          <FileTextOutlined style={{ color: '#3b82f6' }} />
          {text}
        </Space>
      ),
    },
    {
      title: '分块内容',
      dataIndex: 'content',
      key: 'content',
      render: (text) => (
        <Text className="text-sm line-clamp-2" style={{ display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {text}
        </Text>
      ),
    },
    {
      title: '章节路径',
      dataIndex: 'sectionPath',
      key: 'sectionPath',
      width: 160,
      render: (text) => <Text type="secondary" className="text-xs">{text}</Text>,
    },
    {
      title: '位置',
      dataIndex: 'index',
      key: 'index',
      width: 60,
      align: 'center',
      render: (n) => <Tag>#{n + 1}</Tag>,
    },
    {
      title: '相似度',
      dataIndex: 'score',
      key: 'score',
      width: 90,
      align: 'center',
      render: (s) => (
        <Progress
          percent={Math.round((s || 0) * 100)}
          size="small"
          strokeColor={s > 0.9 ? '#52c41a' : s > 0.7 ? '#faad14' : '#ff4d4f'}
          format={() => ''}
        />
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Title level={4} className="m-0">知识库管理</Title>
      </div>

      <Tabs
        items={[
          {
            key: 'upload',
            label: (
              <span><UploadOutlined /> 文档上传</span>
            ),
            children: (
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
                {/* Upload panel */}
                <Card title="上传文档" className="lg:col-span-1">
                  <Space direction="vertical" className="w-full" size="middle">
                    <div>
                      <Text type="secondary" className="text-xs block mb-1">知识库标签</Text>
                      <Input
                        placeholder="输入标签名，如: 集团信息化-HR"
                        value={tagInput}
                        onChange={(e) => setTagInput(e.target.value)}
                        suffix={
                          <Select
                            placeholder="或选择已有标签"
                            value={tags.find(t => t.tag === tagInput)?.tag}
                            onChange={setTagInput}
                            allowClear
                            size="small"
                            style={{ width: 160 }}
                            options={tags.map(t => ({ label: t.tag, value: t.tag }))}
                          />
                        }
                      />
                    </div>

                    <Dragger {...uploadProps} className="w-full">
                      <p className="ant-upload-drag-icon">
                        <InboxOutlined style={{ color: '#3b82f6', fontSize: 40 }} />
                      </p>
                      <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
                      <p className="ant-upload-hint">
                        支持 PDF、Word、PPT、Excel、TXT、HTML、Markdown
                      </p>
                    </Dragger>

                    {uploadState.stage !== 'idle' && (
                      <Card size="small" bodyStyle={{ padding: '12px' }}>
                        <Space direction="vertical" className="w-full">
                          <div className="flex justify-between">
                            <Text type="secondary" className="text-xs">{uploadState.message}</Text>
                            <Text className="text-xs">{uploadState.progress}%</Text>
                          </div>
                          <Progress
                            percent={uploadState.progress}
                            size="small"
                            status={uploadState.stage === 'error' ? 'exception' : uploadState.stage === 'complete' ? 'success' : 'active'}
                            strokeColor={uploadState.stage === 'complete' ? '#52c41a' : '#3b82f6'}
                          />
                          {uploadState.stage === 'complete' && (
                            <Text type="success" className="text-xs">已入库，请刷新分块列表</Text>
                          )}
                        </Space>
                      </Card>
                    )}
                  </Space>
                </Card>

                {/* Git repo upload */}
                <Card
                  title={<Space><GithubOutlined /> Git 仓库导入</Space>}
                  className="lg:col-span-2"
                >
                  <Alert
                    message="Git 仓库分析功能"
                    description="输入 Git 仓库地址，系统将自动克隆并分析所有文档，支持内部文档、技术文档、配置文件等。"
                    type="info"
                    className="mb-4"
                  />
                  <Space.Compact className="w-full">
                    <Input placeholder="https://github.com/username/repo" />
                    <Input placeholder="用户名 (可选)" style={{ width: 120 }} />
                    <Button type="primary" icon={<CloudServerOutlined />}>
                      分析导入
                    </Button>
                  </Space.Compact>
                </Card>
              </div>
            ),
          },
          {
            key: 'tags',
            label: (
              <span><DatabaseOutlined /> 标签管理</span>
            ),
            children: (
              <Card>
                <List
                  loading={loading}
                  dataSource={tags}
                  locale={{ emptyText: '暂无知识库标签' }}
                  renderItem={(item) => (
                    <List.Item
                      actions={[
                        <Button key="view" size="small" type="link">
                          查看分块
                        </Button>,
                        <Popconfirm
                          key="delete"
                          title="确认删除此标签及其所有分块数据?"
                          onConfirm={() => message.success('已删除')}
                        >
                          <Button size="small" danger type="link" icon={<DeleteOutlined />}>
                            删除
                          </Button>
                        </Popconfirm>,
                      ]}
                    >
                      <List.Item.Meta
                        avatar={<DatabaseOutlined style={{ fontSize: 24, color: '#3b82f6' }} />}
                        title={item.tag}
                        description={
                          <Space size="large">
                            <Tag color="blue">{item.documentCount || 0} 个文档</Tag>
                            <Tag color="green">{item.chunkCount || 0} 个分块</Tag>
                          </Space>
                        }
                      />
                    </List.Item>
                  )}
                />
              </Card>
            ),
          },
          {
            key: 'chunks',
            label: (
              <span><FileTextOutlined /> 分块预览</span>
            ),
            children: (
              <Card>
                <div className="flex gap-2 mb-4">
                  <Select
                    placeholder="按标签筛选"
                    allowClear
                    style={{ width: 200 }}
                    options={tags.map(t => ({ label: t.tag, value: t.tag }))}
                    onChange={setSelectedTag}
                  />
                  <Input.Search placeholder="搜索分块内容..." style={{ width: 240 }} />
                </div>
                <Table
                  columns={columns}
                  dataSource={mockChunks}
                  rowKey="chunkId"
                  size="small"
                  pagination={{ pageSize: 10, showSizeChanger: true }}
                />
              </Card>
            ),
          },
        ]}
      />
    </div>
  );
}
