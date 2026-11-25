# 03 二维地图坐标对齐

对应博客《03 双车型地图：两张地图如何对齐》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

给定两张地图上已经配对的站点，把第一张图上的点转到第二张图的坐标系。起步实现用前两对点解旋转和平移，再计算全部点的 RMSE。

手算例子：`(0,0)→(2,-1)`、`(4,0)→(2,3)`。第一张图的横向向量变成第二张图的纵向向量，所以旋转 90°，平移为 `(2,-1)`。

你的任务是用全部对应点做最小二乘。先求两个质心，求去质心后配对向量的点积和、叉积和，用 `atan2(cross, dot)` 求角度，再用两个质心确定平移。保留 RMSE，整组来源点集中或无法解出旋转时应拒绝。进阶用例含有带偏差的对应点、前两点重合但整组仍分散，以及完全重合。这套模型只允许旋转和平移。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyFrameAlignment.java](src/cn/shiinapple/amr/mapalignment/MyFrameAlignment.java) 后重新运行即可。[FrameAlignmentTest.java](src/cn/shiinapple/amr/mapalignment/FrameAlignmentTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
