# checklab · AMR Java 练习

## 简历项目和面试练习

建议先打开 [简历项目学习路线](docs/interview.md)，按简历技术点选题。原有 00–12 继续用各自的 README；[AMR 面试教程](docs/amr.md) 为其中的地图预检、路线搜索、异步下发和批量编辑补充手算示例、独立练习和自测解析。

新增 13–18 各有一组完整可运行的示范和扩展边界用例。进阶用例同样可以直接通过，随后按各题的独立练习修改代码。运行方法见 [macOS 指南](docs/macos.md)。

| 篇号 | Java 练习 | 动手内容 |
| --- | --- | --- |
| 13 | [机器人断点交接](13-checkpoint-handoff-lab/README.md) | 核对路线版本、重复站下标、回站尝试和到站反馈 |
| 14 | [HTTP 到 RPC](14-http-rpc-lab/README.md) | 按公开路由绑定参数，模拟泛化调用和错误响应 |
| 15 | [版本化路由快照](15-route-snapshot-lab/README.md) | 全量配置热更新，拒绝旧版本，保持单请求内一致性 |
| 16 | [令牌桶限流](16-rate-limit-lab/README.md) | 用可控时钟检验突发额度、时间边界和并发扣减 |
| 17 | [MCP 会话与工具](17-mcp-session-lab/README.md) | 用进程内会话练习初始化、工具发现和调用授权 |
| 18 | [异步事件幂等](18-event-idempotency-lab/README.md) | 用积分示例识别重投、冲突和并发重复消费 |

## 原有 AMR 博客练习

这里是按篇号整理的 13 个独立练习。请在本目录的各个子文件夹里修改代码。每个目录都可以单独打开和运行，基于 JDK 17，使用本地构造的数据。

在 Mac 上打开某个练习目录，双击 **RunLab.command** 跑基础测试。在该目录的终端执行 `bash RunLab.command --goal` 可以检查进阶目标。初始实现先通过基础测试，进阶用例会指出需要你改的行为。第 05 篇使用同目录下的 RunMapLab.command，也可运行 ViewComparison.command 查看对比报告。

| 篇号 | Java 练习 | 动手内容 |
| --- | --- | --- |
| 00 | [清扫任务流程](00-mission-flow-lab/README.md) | 从发布地图、导航应答到顺序到站，推进任务状态 |
| 01 | [共享资源与等待规则](01-traffic-rules-lab/README.md) | 保留硬约束，再对可行请求应用返航优先级 |
| 02 | [路线组包与停止边界](02-route-batches-lab/README.md) | 路段索引、动作点、未放行入口和转弯包尾 |
| 03 | [二维地图坐标对齐](03-map-alignment-lab/README.md) | 用对应点拟合旋转、平移和 RMSE |
| 04 | [地图同步提案](04-map-sync-lab/README.md) | 比较本地和远端各自的基线，处理删除和冲突 |
| 05 | [已有的地图路径检查练习](java-map-check-lab/README.md) | 包围盒粗筛和精确检查次数 |
| 06 | [TCP 字节流分帧](06-tcp-framing-lab/README.md) | 处理半包、多帧和二进制正文 |
| 07 | [有向图路径搜索](07-min-hop-path-lab/README.md) | 先比段数，再比距离，按前驱重建路径 |
| 08 | [沿调用链定位失败](08-code-tracing-lab/README.md) | 用请求 ID 串起调用，定位第一条失败记录 |
| 09 | [路线占位与等待图](09-traffic-lookahead-lab/README.md) | 沿折线截取前方占位，识别互相等待的车辆组 |
| 10 | [异步发送与任务恢复](10-route-recovery-lab/README.md) | 防重复发送、按路线下标恢复、重试时读新状态 |
| 11 | [缓存、快照合并与预览校验](11-state-consistency-lab/README.md) | 缓存地图检查、避免旧快照覆盖、复核同步版本 |
| 12 | [批量删除与局部恢复](12-batch-delete-lab/README.md) | 关联路径、在途去重、局部回滚和串行执行 |

[博客原文目录](../../../../personal/shiinapple.cn/blog/articles/amr-index.md)
