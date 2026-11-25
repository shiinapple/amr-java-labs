#!/bin/bash
# 双击后从脚本所在目录编译，避免受终端当前目录影响。
cd "$(dirname "$0")" || exit 1

echo "正在编译地图检查练习……"
if ! command -v javac >/dev/null 2>&1 || ! command -v java >/dev/null 2>&1; then
    echo "找不到 Java 编译器或运行环境，请安装 JDK 17 或更新版本。"
    read -r -p "按回车键关闭窗口……" _
    exit 1
fi
if ! javac -encoding UTF-8 -d out src/lab/*.java; then
    echo "编译失败，请查看上面的错误。"
    read -r -p "按回车键关闭窗口……" _
    exit 1
fi

echo
echo "正在运行正确性测试："
if ! java -cp out lab.PathCheckerTest; then
    echo "正确性测试未通过，请查看上面的错误；仍会打开图形界面供观察。"
fi
echo
echo "正在打开图形界面。关闭图形窗口后可关闭此终端。"
java -cp out lab.MapLabApp
result=$?
if [ "$result" -ne 0 ]; then
    echo "图形界面启动失败，请查看上面的错误。"
    read -r -p "按回车键关闭窗口……" _
fi
exit "$result"
