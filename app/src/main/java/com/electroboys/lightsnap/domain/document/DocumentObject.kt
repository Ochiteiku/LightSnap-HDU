package com.electroboys.lightsnap.domain.document

import com.electroboys.lightsnap.data.entity.Document

object DocumentObject {
    val documents = listOf(
        Document(
            id = "doc1",
            title = "项目需求文档",
            content = """
                核心功能需求：
                1. 用户认证模块
                   - 手机号+验证码登录
                   - 第三方账号绑定
                   - 生物识别支持
                2. 数据可视化看板
                   - 实时数据刷新（WebSocket）
                   - 自定义图表类型
                   - 数据导出PDF/Excel
                3. 消息系统
                   - 未读消息红点标记
                   - 多端同步已读状态
                   - 消息分类过滤
                
                技术指标：
                • 响应时间 < 500ms
                • 支持10万级并发
                • 数据加密传输
            """.trimIndent(),
            time = "2025-05-09"
        ),
        Document(
            id = "doc2",
            title = "产品迭代会议记录",
            content = """
                会议结论：
                1. UI/UX优化方向
                   ✓ 主色调改为科技蓝（#2A5CAA）
                   ✓ 增加交互动效
                   ✓ 优化字体层级
                2. 功能优先级调整
                   [P0] 支付系统重构
                   [P1] 会员成长体系
                   [P2] 皮肤主题商店
                3. 风险项：
                   ! 第三方SDK合规审查
                   ! 安卓端深色模式适配
                
                后续行动：
                • 设计稿周三前交付
                • 技术方案周五评审
            """.trimIndent(),
            time = "2025-05-08"
        ),
        Document(
            id = "doc3",
            title = "Android开发规范",
            content = """
                架构规范：
                1. 分层架构
                   - UI层：Compose + ViewModel
                   - Domain层：UseCase
                   - Data层：Repository
                2. 代码质量
                   ✔ 覆盖率 > 80%
                   ✔ SonarQube静态扫描
                   ✔ 每周Code Review
                
                性能优化：
                • 内存泄漏检测流程：
                  1. LeakCanary监控
                  2. MAT分析hprof
                  3. 弱引用改造
                • 线程管理原则：
                  - 主线程禁止IO
                  - 使用Coroutine+Dispatchers
                  - 避免全局Scope
                
                安全要求：
                ‼ 敏感数据必须加密
                ‼ HTTPS证书强校验
                ‼ ProGuard混淆配置
            """.trimIndent(),
            time = "2025-05-07"
        )
    )
}