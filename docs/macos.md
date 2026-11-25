# macOS 上运行新增练习

新增的 13–18 每个文件夹都可以单独打开和运行。先在 Mac 的终端检查 JDK。

```bash
java -version
javac -version
```

两个命令都应返回 17 或以上。以第 13 题为例，在 Mac 的终端运行：

```bash
cd ~/code/learning/Java/amr/checklab/13-checkpoint-handoff-lab
bash RunLab.command
bash RunLab.command --goal
```

第一条脚本执行教程中的完整示范。带 `--goal` 的调用还执行边界用例。新增练习交付时两种模式均可通过。独立练习要求你在此基础上改输入、补断言或扩展规则。基础练习也可以在 Finder 双击 `RunLab.command`。

脚本每次用 `javac --release 17 -encoding UTF-8` 编译当前源码到新的临时目录，退出时清理。修改代码后重新运行脚本就可以看到结果。新增练习使用 JDK 自带类，不需要 Maven、数据库、其他服务或联网。运行旧题时按各自 README 里的入口，地图路径检查题使用 `RunMapLab.command`。

如果看到 `AssertionError`，先读报错中的中文说明，再到入口类里找到对应断言。对照教程的时序表，确认是输入条件、状态变化还是输出与你手推的结果不一致。

[回到 checklab 索引](../README.md) · [简历项目与面试练习](interview.md)
