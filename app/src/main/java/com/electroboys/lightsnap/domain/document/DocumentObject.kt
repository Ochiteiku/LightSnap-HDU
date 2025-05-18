package com.electroboys.lightsnap.domain.document

import com.electroboys.lightsnap.data.entity.Document

object DocumentObject {
    val documents: List<Document>
        get() {
            val listOf = listOf(
                Document(
                    id = "doc1",
                    title = "项目开发大纲 V3.2",
                    content = """
                            📌 核心功能需求：
                            
                            🔹 用户认证模块（优先级P0）
                            • 多因子认证流程：
                              - 手机号+短信验证码（Twilio集成）
                              - 邮箱+密码+二次验证（Google Authenticator）
                              - 生物识别（FaceID/TouchID/指纹）
                            • 安全防护机制：
                              - 异地登录提醒
                              - 可疑行为风控（设备指纹检测）
                              - JWT令牌自动刷新
                            
                            🔹 数据可视化看板（优先级P1）
                            • 实时数据流处理：
                              - WebSocket长连接保活机制
                              - 数据压缩传输（Protocol Buffers）
                              - 断线自动重连+消息补发
                            • 图表交互功能：
                              - 多维度下钻分析
                              - 时间范围对比（同比/环比）
                              - 自定义预警阈值设置
                            
                            🔹 消息通知系统（优先级P2）
                            • 全渠道推送：
                              - 应用内消息中心
                              - 邮件通知模板
                              - 企业微信/钉钉机器人
                            • 智能分类：
                              - NLP关键词提取
                              - 用户行为关联推荐
                              - 紧急消息强提醒
                            
                            ⚙️ 技术指标：
                            • 性能要求：
                              - API响应时间 < 300ms（P99）
                              - 支持50万级QPS
                              - 冷启动时间 < 1.5s
                            • 数据安全：
                              - TLS 1.3加密传输
                              - 敏感字段AES-256加密存储
                              - 定期安全渗透测试
                            
                            📅 里程碑计划：
                            需求冻结：产品需求文档PRD终版（2025-06-01）
                            原型评审：高保真交互原型Figma（2025-06-15）
                            开发启动：技术架构设计文档（2025-07-01）
                            
                            
                            how are you
                            i'm fine thank you
                            
                            
                            
                        """.trimIndent(),
                    time = "2025-05-20 14:30"
                ),
                Document(
                    id = "doc2",
                    title = "产品迭代会议记录 - Sprint 28",
                    content = """
                            📍 会议概要：
                            • 时间：2025-05-19 10:00-12:30
                            • 地点：线上（Zoom会议室）
                            • 参会人：产品/设计/研发/测试负责人
                            
                            🔎 关键结论：
                            
                            🎨 UI/UX优化方案：
                            ✓ 视觉风格升级：
                              - 主色系：#2A5CAA → #1E4B8B（WCAG 2.1 AA合规）
                              - 动态微交互：
                                • 按钮按压涟漪效果
                                • 页面过渡共享元素动画
                              - 字体系统：
                                • 中文：思源黑体Medium
                                • 英文：Roboto Flex
                            
                            ⚡ 功能迭代计划：
                            [P0] 支付系统重构
                            • 新接入渠道：
                              - 微信支付国际版（Stripe对接）
                              - 加密货币支付（Coinbase Commerce）
                            • 对账流程优化：
                              - 自动差错处理
                              - 多维度报表导出
                            
                            [P1] 会员成长体系
                            • 等级权益设计：
                              - 专属客服通道
                              - 生日特权礼包
                              - 线下活动邀请
                            • 积分规则：
                              - 消费1元=10积分
                              - 每日签到梯度奖励
                            
                            ⚠️ 风险预警：
                            ❗ 第三方依赖风险：
                            - 谷歌地图SDK合规审查（需备案）
                            - Facebook登录SDK需替换（改用Firebase Auth）
                            
                            ❗ 技术债务：
                            - 安卓WebView兼容性问题（需统一内核）
                            - iOS14以下版本渐退支持
                            
                            📌 后续行动项：
                            设计师A：交付支付页面改稿（2025-05-23）
                            开发员B：会员API接口文档编写（2025-05-24）
                            测试员C：编写跨境支付测试用例（025-05-25）
                        """.trimIndent(),
                    time = "2025-05-19 15:45"
                ),
                Document(
                    id = "doc3",
                    title = "Android开发规范 V2.3",
                    content = """
                            🏗️ 架构规范：
                            
                            📐 分层架构设计：
                            • UI层：
                              - 单一Activity架构
                              - Compose组件化开发
                              - ViewModel状态管理
                            • Domain层：
                              - UseCase组合业务逻辑
                              - Flow实现数据流
                            • Data层：
                              - Room+Retrofit组合
                              - 多数据源策略模式
                            
                            ✨ 代码质量保障：
                            ✔ 静态检查：
                              - Detekt静态分析（阈值：0警告）
                              - ktlint格式校验（Git预提交钩子）
                            ✔ 测试覆盖：
                              - 单元测试：JUnit5 + MockK
                              - UI测试：Jetpack Compose测试库
                              - 覆盖率报告（JaCoCo集成）
                            ✔ 协作流程：
                              - 每日Code Review（GitHub PR）
                              - 缺陷跟踪（Jira看板）
                            
                            ⚡ 性能优化指南：
                            🔧 内存管理：
                            1. 内存泄漏检测流程：
                               - LeakCanary 2.9监控
                               - Android Profiler分析
                               - MAT内存快照比对
                            2. 优化策略：
                               - 图片加载（Coil+内存缓存）
                               - 大数据分页加载（Paging 3）
                            
                            🔧 线程规范：
                            - 严格线程约束：
                              Main ：UI操作/轻量计算
                              IO：网络/数据库/文件
                              Default：复杂计算任务
                            - 协程使用原则：
                              • 避免GlobalScope
                              • 结构化并发（CoroutineScope）
                              • 异常处理（CoroutineExceptionHandler）
                            
                            🔒 安全合规要求：
                            ‼ 数据安全：
                            - 敏感信息加密：
                              • 密钥管理（Android KeyStore）
                              • 字段级加密（SQLCipher）
                            - 网络通信：
                              • Certificate Pinning
                              • 禁止明文传输（CleartextTraffic）
                            
                            ‼ 隐私合规：
                            - GDPR数据收集声明
                            - 权限最小化原则
                            - 用户数据删除链路
                        """.trimIndent(),
                    time = "2025-05-18 09:15"
                )
            )
            return listOf
        }
}