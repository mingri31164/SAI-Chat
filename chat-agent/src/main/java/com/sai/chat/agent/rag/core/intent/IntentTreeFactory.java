/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sai.chat.agent.rag.core.intent;

import com.sai.chat.agent.framework.convention.rag.IntentNode;
import com.sai.chat.agent.framework.convention.rag.enums.IntentKind;
import com.sai.chat.agent.framework.convention.rag.enums.IntentLevel;

import java.util.List;

/**
 * 意图树工厂
 * <p>
 * 构建默认的意图树结构。
 * 在实际项目中，意图树应从数据库动态加载，
 * 此处提供内存初始化方式作为演示和开发环境使用。
 */
public class IntentTreeFactory {

    private IntentTreeFactory() {
    }

    /**
     * 构建默认意图树
     * <p>
     * 包含三个顶级领域：
     * - 集团信息化（KB 类型）
     * - 业务系统（KB 类型）
     * - 实时数据（MCP 类型）
     */
    public static List<IntentNode> buildDefaultTree() {
        // ========== 集团信息化 ==========
        IntentNode groupInfo = IntentNode.builder()
                .id("group-info")
                .name("集团信息化")
                .level(IntentLevel.DOMAIN)
                .kind(IntentKind.KB)
                .build();

        IntentNode hr = IntentNode.builder()
                .id("group-hr")
                .name("人事")
                .level(IntentLevel.CATEGORY)
                .parentId("group-info")
                .kind(IntentKind.KB)
                .description("招聘、入职、转正、离职、绩效、薪资、考勤、请假等人力资源相关问题")
                .examples(List.of("请假流程是怎样的？", "试用期多久转正？"))
                .build();

        IntentNode hrIntro = IntentNode.builder()
                .id("group-hr-intro")
                .name("人事介绍")
                .level(IntentLevel.TOPIC)
                .parentId("group-hr")
                .kind(IntentKind.KB)
                .collectionName("kb_group_hr_intro")
                .topK(10)
                .description("人事部门整体介绍，包括职责分工、联系方式等")
                .examples(List.of("人事部是做什么的？", "人事部联系方式"))
                .build();

        IntentNode hrLeave = IntentNode.builder()
                .id("group-hr-leave")
                .name("请假流程")
                .level(IntentLevel.TOPIC)
                .parentId("group-hr")
                .kind(IntentKind.KB)
                .collectionName("kb_group_hr_leave")
                .topK(10)
                .description("请假申请流程、审批规则、假期类型说明")
                .examples(List.of("怎么请假？", "年假有多少天？"))
                .build();

        IntentNode itSupport = IntentNode.builder()
                .id("group-it")
                .name("IT支持")
                .level(IntentLevel.CATEGORY)
                .parentId("group-info")
                .kind(IntentKind.KB)
                .description("VPN配置、邮箱设置、软件安装、网络问题等 IT 运维支持")
                .examples(List.of("VPN怎么配置？", "邮箱密码忘了怎么办？"))
                .build();

        IntentNode finance = IntentNode.builder()
                .id("group-finance")
                .name("财务")
                .level(IntentLevel.CATEGORY)
                .parentId("group-info")
                .kind(IntentKind.KB)
                .description("报销、发票、预算、成本等财务相关问题")
                .examples(List.of("报销流程是什么？", "发票怎么开？"))
                .build();

        hr.setChildren(List.of(hrIntro, hrLeave));
        groupInfo.setChildren(List.of(hr, itSupport, finance));

        // ========== 业务系统 ==========
        IntentNode biz = IntentNode.builder()
                .id("biz")
                .name("业务系统")
                .level(IntentLevel.DOMAIN)
                .kind(IntentKind.KB)
                .build();

        IntentNode oa = IntentNode.builder()
                .id("biz-oa")
                .name("OA系统")
                .level(IntentLevel.CATEGORY)
                .parentId("biz")
                .kind(IntentKind.KB)
                .description("OA 办公自动化系统，包括审批流程、公文管理等")
                .build();

        IntentNode oaIntro = IntentNode.builder()
                .id("biz-oa-intro")
                .name("系统介绍")
                .level(IntentLevel.TOPIC)
                .parentId("biz-oa")
                .kind(IntentKind.KB)
                .collectionName("kb_biz_oa_intro")
                .topK(10)
                .description("OA 办公自动化系统的整体介绍")
                .examples(List.of("OA系统是什么？", "OA系统有哪些功能？"))
                .build();

        IntentNode oaSecurity = IntentNode.builder()
                .id("biz-oa-security")
                .name("数据安全")
                .level(IntentLevel.TOPIC)
                .parentId("biz-oa")
                .kind(IntentKind.KB)
                .collectionName("kb_biz_oa_security")
                .topK(10)
                .description("OA 系统的数据安全管理规范")
                .examples(List.of("OA数据安全规范是什么？", "敏感数据怎么保护？"))
                .build();

        IntentNode oaManual = IntentNode.builder()
                .id("biz-oa-manual")
                .name("操作手册")
                .level(IntentLevel.TOPIC)
                .parentId("biz-oa")
                .kind(IntentKind.KB)
                .collectionName("kb_biz_oa_manual")
                .topK(10)
                .description("OA 系统各模块的操作手册和使用指南")
                .examples(List.of("OA怎么用？", "审批流程怎么走？"))
                .build();

        IntentNode ins = IntentNode.builder()
                .id("biz-ins")
                .name("保险系统")
                .level(IntentLevel.CATEGORY)
                .parentId("biz")
                .kind(IntentKind.KB)
                .description("互联网保险系统的业务流程和规范")
                .build();

        IntentNode insSecurity = IntentNode.builder()
                .id("biz-ins-security")
                .name("数据安全")
                .level(IntentLevel.TOPIC)
                .parentId("biz-ins")
                .kind(IntentKind.KB)
                .collectionName("kb_biz_ins_security")
                .topK(10)
                .description("保险系统的数据安全管理规范")
                .examples(List.of("保险系统数据安全规范是什么？"))
                .build();

        oa.setChildren(List.of(oaIntro, oaSecurity, oaManual));
        ins.setChildren(List.of(insSecurity));
        biz.setChildren(List.of(oa, ins));

        // ========== MCP 实时数据 ==========
        IntentNode sales = IntentNode.builder()
                .id("sales")
                .name("销售汇总数据统计")
                .level(IntentLevel.DOMAIN)
                .kind(IntentKind.MCP)
                .build();

        IntentNode salesData = IntentNode.builder()
                .id("sales-data")
                .name("销售数据统计")
                .level(IntentLevel.CATEGORY)
                .parentId("sales")
                .kind(IntentKind.MCP)
                .mcpToolId("sales_query")
                .description("查询销售数据，支持按地区、时间、产品维度统计")
                .examples(List.of("本月销售总额是多少？", "华东地区企业版的销量"))
                .build();

        sales.setChildren(List.of(salesData));

        // ========== SYSTEM 系统交互 ==========
        IntentNode sys = IntentNode.builder()
                .id("sys")
                .name("系统交互")
                .level(IntentLevel.DOMAIN)
                .kind(IntentKind.SYSTEM)
                .build();

        IntentNode sysWelcome = IntentNode.builder()
                .id("sys-welcome")
                .name("欢迎与问候")
                .level(IntentLevel.CATEGORY)
                .parentId("sys")
                .kind(IntentKind.SYSTEM)
                .promptTemplate("你好！我是企业智能助手，可以帮你查询知识库内容、进行销售数据统计等。有什么可以帮到你的？")
                .description("用户打招呼、询问助手身份等问题")
                .examples(List.of("你好", "你是谁？", "有什么可以帮助你的？"))
                .build();

        sys.setChildren(List.of(sysWelcome));

        return List.of(groupInfo, biz, sales, sys);
    }
}
