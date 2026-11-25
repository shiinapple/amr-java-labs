#!/bin/bash
set -eu

cd "$(dirname "$0")"

if ! command -v javac >/dev/null || ! command -v java >/dev/null; then
    echo "请安装 JDK 17 或更新版本。"
    exit 1
fi

build_dir="$(mktemp -d "${TMPDIR:-/tmp}/checklab.XXXXXX")"
trap 'rm -rf "$build_dir"' EXIT

javac --release 17 -encoding UTF-8 -d "$build_dir" src/cn/shiinapple/events/idempotency/*.java
java -cp "$build_dir" cn.shiinapple.events.idempotency.EventIdempotencyLab "$@"
