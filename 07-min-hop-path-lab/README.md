# 07 有向图路径搜索

对应博客《07 路径规划与地图编辑：从搜索到批量删除》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

从 A 找到 D，业务规则先比较路段数，再比较同段数方案的总长度。博客里 A→B→D 是 2 段 4 米，A→C→D 是 2 段 5 米。如果又有 A→D 一段 10 米，仍优先选这一段。第 12 篇练习覆盖本篇的批量删除部分。

起步实现用广度优先搜索找最少段数，仍受邻接表顺序影响。你的任务是保留方向、可达性和最少段数，在同段数时比较距离，并把实际走过的路径 ID 一并返回。可用 `PriorityQueue` 按 `(段数, 距离)` 排序，在发现更好的代价时更新前驱。测试使用非负整数米数，完全相同的代价可任选一条。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyPathPlanner.java](src/cn/shiinapple/amr/minhoppath/MyPathPlanner.java) 后重新运行即可。[PathPlannerTest.java](src/cn/shiinapple/amr/minhoppath/PathPlannerTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
