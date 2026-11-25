# 08 沿调用链定位失败

对应博客《08 代码索引：如何追踪关键实现与演进》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

这一章练的是查证。测试日志模拟浏览器、网关、交通模块和控制器在同一时间处理不同请求。每行都有 `requestId`、`caller`、`method`、`input`、`output`，可以回答谁调用它、读了什么、结果交给谁。

先用 `inspect` 读只含一个请求的例子，再看两次请求交错的日志。你的任务是沿同一个请求 ID 只收集该请求的调用，并定位第一条失败记录。最后的反馈或者日志收尾可能成功，报告里仍应保留更早的失败点。找不到请求时返回空轨迹、空失败点。在真实仓库阅读时，可用类似方法从调用方追到被调用方法，再找对应测试或历史 diff。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyCallTrace.java](src/cn/shiinapple/amr/codetracing/MyCallTrace.java) 后重新运行即可。[CallTraceTest.java](src/cn/shiinapple/amr/codetracing/CallTraceTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
