<p align="center">
  <img width="128px" src="https://github.com/mingri31164/SAI-Chat/blob/main/docs/.github/sai-logo.png" />
</p>
<h1 align="center">SAI林语</h1>
<p align="center">该代码仓库为 SAI林语聊天室 后端相关代码</p>
<p align="center">客户端地址：https://github.com/DWHengr/linyu-client</p>

# 简绍

`SAI-Chat`是基于`tauri`开发的桌面聊天软件，前端框架使用`react`，后端框架使用`springboot`进行开发
，管理端使用`vue3`进行开发。其中使用http和websocket实现消息发送和推送，使用webrtc实现音视频聊天。

# 目前功能

## 客户端功能

好友相关、朋友圈、音视频聊天、语音消息、文本消息、文件消息、图片消息、截图、群聊等。

## 管理端功能

登录信息统计、用户管理、在线聊天、系统通知管理、第三方会话管理、动态线程池管理。


## 技术选型

| 技术选型            | 说明      |
|-----------------|---------|
| SpringBoot      | 脚手架     |
| SpringAI        | 大模型集成框架 |
| Spring Security | 权限框架    |
| Mybatis-Plus    | ORM框架   |
| MySQL           | 关系型数据库  |
| Redis           | 缓存      |
| Caffeine        | 本地缓存    |
| RocketMQ        | 消息队列    |
| Netty           | 网络通信框架  |
| SLF4J           | 日志框架    |


## 项目架构

```Bash
SAI-Chat
├── chat-ai # 大模型应用模块，如RAG、MCP等
├── chat-api # 通用的枚举、实体类定义
├── chat-core # 核心组件模块，如配置、工具类等
├── chat-frameworks # 自定义组件模块，如线程池、事件监听组件等
├── chat-service # 业务相关的主要逻辑
└── chat-web # 项目启动入口，Web模块
```



# 客户端截图

## 登录

![1](https://github.com/user-attachments/assets/0cccc2d1-79c8-43fd-844f-9254edbe6e7e)

## 聊天

![2](https://github.com/user-attachments/assets/0d3d85be-1342-4bd2-b4f1-614c93a8a0a5)

## 群聊

![3](https://github.com/user-attachments/assets/6aa0a021-92b7-46fe-8aea-5487d97362a7)

## 通讯列表

![4](https://github.com/mingri31164/SAI-Chat/blob/main/docs/.github/friend.png)

## 朋友圈

![5](https://github.com/user-attachments/assets/b30432b9-904a-432c-bb85-03f8560ddc3b)

## 通知

![6](https://github.com/user-attachments/assets/b7eb922d-9aec-4607-b004-6921e178facb)

# 管理端截图

## 首页

![7](https://github.com/user-attachments/assets/cbca1555-53a0-4107-90ea-25e7f9f441e4)

## 在线聊天

![8](https://github.com/user-attachments/assets/acb99729-48d4-47cf-b837-9fcac7221c5d)

## 用户管理

![9](https://github.com/user-attachments/assets/afa3b6de-54f9-4927-9fd5-f5e97dcb8884)

## 系统通知管理

![10](https://github.com/user-attachments/assets/fff0cb8e-0339-4df7-9935-bc552b788e9e)

## 后台线程池管理

![11](https://github.com/mingri31164/SAI-Chat/blob/main/docs/.github/threadpool.png)

## AI Agent智能体

![12](https://github.com/mingri31164/SAI-Chat/blob/main/docs/.github/rag.png)
