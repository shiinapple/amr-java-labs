# 01 共享资源与等待规则

对应博客《01 交通管制：多辆车争用通道时如何协调》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

两辆模拟车同时请求一个起点。起点内的车需要先离开，返航车也可能因为业务优先级要求另一辆让路。用 `decide` 输出每台车的等待原因。

手算例子：A 是起点资源持有者，B 是正在返航的请求方。B 已经被占用规则挡住，此时再让 A 因返航优先等 B，会形成互相等待。把优先级应用到仍符合条件的请求者，保留资源占用和外部障碍两条硬约束。本练习只处理一处共享资源，通道轮次和现场许可由业务系统另行维护。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyTrafficRules.java](src/cn/shiinapple/amr/trafficrules/MyTrafficRules.java) 后重新运行即可。[TrafficRulesTest.java](src/cn/shiinapple/amr/trafficrules/TrafficRulesTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
