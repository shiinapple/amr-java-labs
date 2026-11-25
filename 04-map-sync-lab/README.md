# 04 地图同步提案

对应博客《04 地图同步：如何识别本地与控制器的冲突》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

系统要判断站点差异该建议新增、更新、保留还是交给人判断。这里的字符串 V1、V2 表示已归一化的站点内容。返回值是预览提案，测试不会写入地图。

`Baseline(local, remote)` 是上次同步留下的两份独立基线。参数为 `null` 时表示没有同步记录，记录里的单个 `null` 表示那一端当时没有该站。

- 两边现在相同，或远端保持原样，先处理这些明确的情况。
- 自动建议更新要求本地没变且两端旧基线原来一致。即使本地没有变，也不能擅自替用户解决上次就有的分歧。
- 远端删了曾经存在的站点时，提案携带本地引用清单；有歧义的对应关系先阻止。真正提交时的版本复核见第 11 篇练习。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyMapSync.java](src/cn/shiinapple/amr/mapsync/MyMapSync.java) 后重新运行即可。[MapSyncTest.java](src/cn/shiinapple/amr/mapsync/MapSyncTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
