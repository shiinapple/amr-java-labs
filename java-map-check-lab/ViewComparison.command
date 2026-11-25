#!/bin/bash
cd "$(dirname "$0")" || exit 1
if ! command -v javac >/dev/null 2>&1 || ! command -v java >/dev/null 2>&1; then
    echo "请安装 JDK 17 或更新版本。"
    read -r -p "按回车关闭……" _
    exit 1
fi
echo "正在编译并运行比较，请稍候……"
if ! javac -encoding UTF-8 -d out src/lab/*.java || ! java -cp out lab.ComparisonReport; then
    echo "生成失败，请查看上面的错误。"
    read -r -p "按回车关闭……" _
    exit 1
fi
open out/comparison.html
