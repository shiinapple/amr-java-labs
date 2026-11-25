# 06 TCP 字节流分帧

对应博客《06 实时通信：从 TCP 分帧到页面状态一致》。数据由练习在本地构造，课堂模型与博客中的 TypeScript 业务实现有简化差异。

输入是一次从网络读到的任意字节块，输出是这次刚收完整的所有正文。为方便手算，练习帧头使用 **2 字节无符号大端长度**，正文上限 4096 字节，允许长度为 0。博客中的真实帧头还有其他字段。

例如 `[0, 3, 65, 66, 67]` 表示 3 字节正文 `ABC`。第一次读到 `[0]`，第二次收到 `[3, 65]` 时仍要继续等，收到 `[66, 67]` 才输出正文。

你的任务是让 `feed` 跨调用保留未收齐的字节，并在同一块里循环取出多帧。先检查头，再检查长度和剩余数据。非法长度重置缓冲并抛错。正文按字节返回，可以是二进制，也可以在解帧以后再转 UTF-8。页面的快照合并见第 11 篇练习。

## 在 Mac 上运行

需要 JDK 17 或更新版本。在 Finder 双击 **RunLab.command** 可以运行基础测试。也可以在终端进入本目录执行：

```bash
bash RunLab.command
bash RunLab.command --goal
```

第一条命令应该通过；第二条会列出进阶目标，初始代码至少有一项未通过。修改 [MyFrameDecoder.java](src/cn/shiinapple/amr/tcpframing/MyFrameDecoder.java) 后重新运行即可。[FrameDecoderTest.java](src/cn/shiinapple/amr/tcpframing/FrameDecoderTest.java) 用固定输入与预期输出做验收。脚本每次重新编译，不需要 Maven、网络或业务仓库。
