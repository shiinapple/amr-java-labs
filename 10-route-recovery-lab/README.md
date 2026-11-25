# 10 异步发送与任务恢复

对应博客《10 路线进阶：异步发送与任务恢复》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

这份练习用可手动完成的 `CompletableFuture` 模拟一次还在等应答的发送。测试在同一个线程内驱动所有事件，所以时间线稳定，可以逐步重现问题。

1. 第一次 `pump` 等应答时调用第二次。给待发送批次做在途登记，重复调用返回 `false`。成功后推进 `sent`，失败后解除在途登记，保持已确认发送的位置。这里的失败测试使用明确拒绝，网络超时还需要业务核对。
2. 路线 A→B→C→B→D 在第二次经过 B 时停下，应从路线下标 3 恢复。用快照里的序号检查站点和路线，路线改过则拒绝。恢复仅返回暂停状态和剩余路线。
3. 模拟数据库忙后，等待重试期间进度从 B 到 C。把读取最新进度放到重试执行时。这里用函数模拟写入，便于手推与复现。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyRouteRecovery.java](src/cn/shiinapple/amr/routerecovery/MyRouteRecovery.java) 后重新运行即可。[RouteRecoveryTest.java](src/cn/shiinapple/amr/routerecovery/RouteRecoveryTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
