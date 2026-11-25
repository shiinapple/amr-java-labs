# 11 缓存、快照合并与预览校验

对应博客《11 一致性进阶：缓存、旧快照与预览校验》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

练习里有三个相互独立的小类，建议分开改，每次跑一次进阶测试。

1. **展示缓存**。同车型同地图内容的展示检查只算一次；车辆在线状态每次读取。若同名地图的路径变了，应重算。模拟启动预检每次都重算，用实际计算次数验收。
2. **旧快照合并**。请求前记住 `startedAt`。等待期间逐 ID 记录新增、更新和删除的修订号。合并后保护这些对象的最新状态，快照仍可更新没发生变更的 ID 和移除已不存在的旧项。
3. **预览校验**。快照之外的另一种冲突是用户确认旧预览。提交前先看本地版本，重新读取远端，再看本地是否在读取期间变化。用内存版本和同步回调模拟这三个时刻，便于固定执行顺序。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyStateConsistency.java](src/cn/shiinapple/amr/stateconsistency/MyStateConsistency.java) 后重新运行即可。[StateConsistencyTest.java](src/cn/shiinapple/amr/stateconsistency/StateConsistencyTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
