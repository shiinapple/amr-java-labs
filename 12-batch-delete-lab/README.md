# 12 批量删除与局部恢复

对应博客《12 编辑进阶：批量删除与局部恢复》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

模拟地图有站点 A、B、C 和路径 A→B、B→C。删 B 时先算出候选站点和关联路径，页面先隐藏它们，一个批次只向假服务提交一次。

起步实现支持串行提交，但失败时恢复整个旧快照，也会把在途 ID 发第二次。你的任务是：

- 同步登记在途的站点和路径 ID，下一批先扣除已在途对象。空批次立即结束，每次结算释放本批记录。
- 失败时只把本批隐藏且当前仍缺失的对象补回，保留等待期间新增和更新的对象。当前缺少路径端点时先别放回悬空路径。
- 保留失败批次返回给调用者的异常，并保持后续批次仍可执行。

测试用手动完成的 Future 重现请求间隔，放在一个线程中执行。已给出的输入只模拟同一张地图，两个在途批次不会各自删除同一条路径的不同端点。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyBatchDelete.java](src/cn/shiinapple/amr/batchdelete/MyBatchDelete.java) 后重新运行即可。[BatchDeleteTest.java](src/cn/shiinapple/amr/batchdelete/BatchDeleteTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
