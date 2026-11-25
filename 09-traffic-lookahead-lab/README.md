# 09 路线占位与等待图

对应博客《09 交通进阶：路线占位与等待图》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

用两个独立方法练习交通分析。输出是模拟占用线段和循环等待分组，供观察和检查。

1. 给当前位置所在段，以及沿路线要占用的长度。用折线 `(0,0)→(4,0)→(4,4)` 从 `(3,0)` 向前取 3 米时，先取 1 米到拐角，再竖直取 2 米。实现每走完一段扣掉剩余长度，零长度边跳过。
2. 等待图中 `A→B` 表示 A 等 B。例子 `A→X, A→B, B→C, C→A` 中，X 虽然是 A 的首要等待原因，真正成环的互相可达组却是 A、B、C。程序要遍历全部等待原因并返回分组；可先练习双向可达，再对照博客中的 Tarjan。小练习只输出多于一个成员的组，不做释放或优先级决策。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyTrafficLookahead.java](src/cn/shiinapple/amr/trafficlookahead/MyTrafficLookahead.java) 后重新运行即可。[TrafficLookaheadTest.java](src/cn/shiinapple/amr/trafficlookahead/TrafficLookaheadTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
