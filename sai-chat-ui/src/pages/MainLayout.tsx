import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Avatar, Dropdown, Tag, Typography } from 'antd';
import {
  RobotOutlined,
  DashboardOutlined,
  MessageOutlined,
  DatabaseOutlined,
  ExperimentOutlined,
  SettingOutlined,
  SafetyOutlined,
  ToolOutlined,
  LogoutOutlined,
  UserOutlined,
  SwapOutlined,
  LineChartOutlined,
  ApartmentOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '@/stores/authStore';
import { UserRole } from '@/types/document';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

const menuItems = [
  {
    key: '/chat',
    icon: <MessageOutlined />,
    label: '智能问答',
  },
  {
    key: '/knowledge',
    icon: <DatabaseOutlined />,
    label: '知识库管理',
  },
  {
    key: '/intent-tree',
    icon: <ApartmentOutlined />,
    label: '意图树',
  },
  {
    key: '/dashboard',
    icon: <DashboardOutlined />,
    label: '监控仪表盘',
  },
  {
    key: 'admin-group',
    icon: <ToolOutlined />,
    label: '管理后台',
    children: [
      {
        key: '/admin/abtest',
        icon: <SwapOutlined />,
        label: 'A/B 测试',
      },
      {
        key: '/admin/prompt',
        icon: <SafetyOutlined />,
        label: 'Prompt 调优',
      },
      {
        key: '/admin/observe',
        icon: <LineChartOutlined />,
        label: '可观测性',
      },
      {
        key: '/admin/review',
        icon: <ExperimentOutlined />,
        label: '复盘分析',
      },
      {
        key: '/admin/experiment',
        icon: <SettingOutlined />,
        label: '实验管理',
      },
    ],
  },
];

const roleLabels: Record<UserRole, { label: string; color: string }> = {
  [UserRole.ADMIN]: { label: '管理员', color: 'red' },
  [UserRole.USER]: { label: '普通用户', color: 'blue' },
  [UserRole.VIEWER]: { label: '访客', color: 'green' },
  [UserRole.GUEST]: { label: '游客', color: 'default' },
};

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();
  const [collapsed, setCollapsed] = useState(false);

  const selectedKey = menuItems
    .flatMap((item) => (item.children ? [item.key, ...item.children.map((c) => c.key)] : [item.key]))
    .find((key) => location.pathname.startsWith(key));

  const handleMenuClick = ({ key }: { key: string }) => {
    if (key === 'admin-group') return;
    navigate(key);
  };

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: `角色: ${roleLabels[user?.role || UserRole.GUEST].label}`,
      disabled: true,
    },
    { type: 'divider' as const },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      danger: true,
      onClick: () => {
        logout();
        navigate('/login');
      },
    },
  ];

  return (
    <Layout className="min-h-screen">
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        width={220}
        className="shadow-md"
        theme="light"
        style={{ borderRight: '1px solid #f0f0f0' }}
      >
        <div
          className="flex items-center justify-center h-16 px-4 border-b border-gray-100"
          style={{ overflow: 'hidden' }}
        >
          {collapsed ? (
            <RobotOutlined style={{ fontSize: 24, color: '#3b82f6' }} />
          ) : (
            <div className="flex items-center gap-2">
              <RobotOutlined style={{ fontSize: 24, color: '#3b82f6' }} />
              <span className="font-semibold text-base text-gray-800 whitespace-nowrap">SAI-Chat</span>
            </div>
          )}
        </div>
        <Menu
          mode="inline"
          selectedKeys={[selectedKey || '']}
          defaultOpenKeys={['admin-group']}
          items={menuItems}
          onClick={handleMenuClick}
          style={{ borderRight: 0, marginTop: 8 }}
        />
      </Sider>

      <Layout>
        <Header
          className="flex items-center justify-between px-6 shadow-sm"
          style={{ borderBottom: '1px solid #f0f0f0', lineHeight: 'normal' }}
        >
          <Text type="secondary" className="text-sm">
            {location.pathname === '/chat' && 'RAG 智能问答 · 多路检索 · ReAct Agent'}
            {location.pathname === '/knowledge' && '知识库管理 · 文档上传 · 分块预览'}
            {location.pathname === '/intent-tree' && '意图树编辑器 · 节点配置 · MCP 工具关联'}
            {location.pathname === '/dashboard' && '实时监控 · 指标仪表盘'}
            {location.pathname.startsWith('/admin/abtest') && 'A/B 测试管理 · 流量分配 · 统计分析'}
            {location.pathname.startsWith('/admin/prompt') && 'Prompt 调优 · 版本管理 · 变体测试'}
            {location.pathname.startsWith('/admin/observe') && '可观测性 · 调用链路 · 评测中心'}
            {location.pathname.startsWith('/admin/review') && '复盘分析 · 成功模式 · 失败教训'}
            {location.pathname.startsWith('/admin/experiment') && '实验管理 · 灰度发布 · 性能优化'}
          </Text>

          <div className="flex items-center gap-3">
            <Tag color="blue">v1.0.0</Tag>
            {user && (
              <>
                <Tag color={roleLabels[user.role].color}>{roleLabels[user.role].label}</Tag>
                <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
                  <div className="flex items-center gap-2 cursor-pointer">
                    <Avatar size="small" style={{ backgroundColor: '#3b82f6' }} icon={<UserOutlined />} />
                    <Text strong>{user.username}</Text>
                  </div>
                </Dropdown>
              </>
            )}
          </div>
        </Header>

        <Content className="p-6 bg-gray-50">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
