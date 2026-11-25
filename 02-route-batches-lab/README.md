# 02 路线组包与停止边界

对应博客《02 路线执行：从连续导航到断点恢复》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

清扫路线 A→B→C→D 有三段，默认一次取两段。一次应答后需要等反馈确认走完该包，才能取下一包。

`sent`、`done` 都用已发和已完成的路段数量表示，同时可当站点下标使用。`nextActionAt` 是下一处未处理动作的站点下标，`blockedLeg` 是禁止跨越的路段下标，两者都可作为本包的右边界。列表里的转弯标记表示不能把该站当包尾。用路线末站、动作和通道约束设定上限，再尝试选一个合法包尾。若刚好卡在转弯，先试向后并入，再在硬边界内向前找。练习省略实际动作执行、网络及允许导航追加的例外。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyRouteBatches.java](src/cn/shiinapple/amr/routebatches/MyRouteBatches.java) 后重新运行即可。[RouteBatchesTest.java](src/cn/shiinapple/amr/routebatches/RouteBatchesTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
