import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Input, Button, Card, Typography, Tabs, message } from 'antd';
import { RobotOutlined, UserOutlined, ArrowRightOutlined } from '@ant-design/icons';
import { useAuthStore } from '@/stores/authStore';
import { UserRole } from '@/types/document';

const { Title, Text } = Typography;

const features = [
  { title: 'RAG 智能问答', desc: '多路检索 · 意图识别 · 问题改写', color: '#3b82f6' },
  { title: 'ReAct Agent', desc: '思考-执行-观察 · 9 状态机', color: '#8b5cf6' },
  { title: 'MCP 工具调用', desc: '销售数据 · 天气查询 · 工单管理', color: '#f59e0b' },
  { title: '三层记忆管理', desc: '滑动窗口 · LLM 摘要 · 自学习', color: '#10b981' },
  { title: 'A/B 测试体系', desc: '流量分配 · 统计显著性检验', color: '#ef4444' },
  { title: 'Prompt 自动调优', desc: '5 种策略 · 迭代优化 · 版本管理', color: '#06b6d4' },
];

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const handleLogin = async (values: { username: string; role: UserRole }) => {
    setLoading(true);
    await new Promise((r) => setTimeout(r, 500)); // simulate network
    login(values.username, values.role);
    message.success(`欢迎回来，${values.username}！`);
    navigate('/chat');
    setLoading(false);
  };

  const quickLogin = (username: string, role: UserRole) => {
    login(username, role);
    message.success(`已以 ${username} 身份登录`);
    navigate('/chat');
  };

  return (
    <div className="min-h-screen flex bg-gradient-to-br from-blue-50 via-white to-purple-50">
      {/* Left panel */}
      <div className="hidden lg:flex flex-col justify-center w-1/2 px-16 bg-white border-r border-gray-100">
        <div className="flex items-center gap-3 mb-8">
          <div className="w-12 h-12 rounded-xl bg-blue-500 flex items-center justify-center shadow-lg shadow-blue-200">
            <RobotOutlined style={{ fontSize: 28, color: 'white' }} />
          </div>
          <div>
            <Title level={2} style={{ margin: 0, color: '#1e293b' }}>
              SAI-Chat
            </Title>
            <Text type="secondary">RAG 智能问答平台</Text>
          </div>
        </div>

        <Title level={4} style={{ color: '#374151' }}>
          项目亮点
        </Title>

        <div className="space-y-4 mt-4">
          {features.map((f) => (
            <div key={f.title} className="flex items-start gap-3">
              <div
                className="w-2 h-2 rounded-full mt-2 flex-shrink-0"
                style={{ backgroundColor: f.color }}
              />
              <div>
                <Text strong style={{ color: '#1e293b' }}>
                  {f.title}
                </Text>
                <br />
                <Text type="secondary" className="text-sm">
                  {f.desc}
                </Text>
              </div>
            </div>
          ))}
        </div>

        <div className="mt-10 p-4 bg-blue-50 rounded-xl border border-blue-100">
          <Text type="secondary" className="text-sm">
            <Text strong type="secondary">
              技术栈
            </Text>
            ：Spring Boot 3.2 + MyBatis-Plus + pgvector · Sa-Token · Redis · 阿里百炼
            DeepSeek-V3 · Ollama · SiliconFlow · MCP 协议
          </Text>
        </div>
      </div>

      {/* Right panel - login form */}
      <div className="flex flex-col justify-center w-full lg:w-1/2 px-8 lg:px-20">
        <Card
          className="shadow-xl shadow-blue-100/50 max-w-md mx-auto w-full"
          styles={{ body: { padding: '40px 32px' } }}
        >
          <div className="text-center mb-8">
            <div className="w-16 h-16 rounded-full bg-blue-500 flex items-center justify-center mx-auto mb-4 shadow-lg shadow-blue-200">
              <RobotOutlined style={{ fontSize: 36, color: 'white' }} />
            </div>
            <Title level={3} style={{ margin: 0 }}>
              登录 SAI-Chat
            </Title>
            <Text type="secondary">RAG 智能问答 · Agent 平台</Text>
          </div>

          <Form
            form={form}
            layout="vertical"
            onFinish={handleLogin}
            initialValues={{ username: '', role: UserRole.USER }}
          >
            <Form.Item
              name="username"
              label="用户名"
              rules={[{ required: true, message: '请输入用户名' }]}
            >
              <Input
                prefix={<UserOutlined style={{ color: '#bfbfbf' }} />}
                placeholder="请输入用户名"
                size="large"
                autoComplete="username"
              />
            </Form.Item>

            <Form.Item name="role" label="角色（演示用）">
              <Tabs
                items={[
                  {
                    key: String(UserRole.USER),
                    label: <span>普通用户</span>,
                    children: null,
                  },
                  {
                    key: String(UserRole.ADMIN),
                    label: <span>管理员</span>,
                    children: null,
                  },
                  {
                    key: String(UserRole.VIEWER),
                    label: <span>访客</span>,
                    children: null,
                  },
                ]}
                onChange={(key) => form.setFieldValue('role', key as UserRole)}
                defaultActiveKey={String(UserRole.USER)}
              />
            </Form.Item>

            <Form.Item style={{ marginBottom: 0 }}>
              <Button
                type="primary"
                htmlType="submit"
                size="large"
                loading={loading}
                block
                icon={<ArrowRightOutlined />}
                className="mt-2"
              >
                进入平台
              </Button>
            </Form.Item>
          </Form>

          <div className="mt-6 text-center">
            <Text type="secondary" className="text-xs">
              快速演示
            </Text>
            <div className="flex gap-2 justify-center mt-2">
              <Button size="small" onClick={() => quickLogin('面试官', UserRole.ADMIN)}>
                管理员
              </Button>
              <Button size="small" onClick={() => quickLogin('张三', UserRole.USER)}>
                普通用户
              </Button>
              <Button size="small" onClick={() => quickLogin('访客', UserRole.VIEWER)}>
                访客
              </Button>
            </div>
          </div>
        </Card>

        <Text type="secondary" className="text-center mt-6 text-xs">
          Mock 登录模式 · 仅用于面试演示
        </Text>
      </div>
    </div>
  );
}
