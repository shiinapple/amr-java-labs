# 00 清扫任务流程

对应博客《00 走进系统：一次清扫任务经过哪些环节》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

一次 A→B→C 的模拟清扫经过地图发布、任务下发和控制器反馈。先看 `MyMissionFlow` 的 `Phase`，再看测试怎样按顺序调用各层。

- `editMap`、`publishMap` 分别表示本地编辑和模拟发布。收到有效路线后，`start` 进入待应答状态。
- 基础用例包含地图修改后重新发布、关闭页面后任务状态仍保留、拒绝地图上没有的路段。
- 你的任务是区分 `AWAITING_ACK`、`RUNNING` 和 `COMPLETED`。完成反馈必须来自当前任务，且要按路线推进；不能从 A 收到一个 C 就跳过 B。初始实现把 ACK 提前记成完成，先从这个分支改起。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyMissionFlow.java](src/cn/shiinapple/amr/missionflow/MyMissionFlow.java) 后重新运行即可。[MissionFlowTest.java](src/cn/shiinapple/amr/missionflow/MissionFlowTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
