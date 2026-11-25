package cn.shiinapple.gateway.routing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static cn.shiinapple.gateway.routing.RouteTable.*;

public final class RouteSnapshotLab {
    public static void main(String[] args) throws InterruptedException {
        var table = new RouteTable();
        check(table.replace(snapshot(1, "node-a")), "发布 v1");
        var oldRequest = table.snapshot();
        check(table.replace(snapshot(3, "node-c")), "跳到最新全量版本 v3");
        check(!table.replace(snapshot(2, "node-b")), "旧事件不覆盖新版本");
        check(oldRequest.resolve("/orders").orElseThrow().endpoint().equals("node-a"), "旧请求快照一致");
        check(table.snapshot().resolve("/orders").orElseThrow().endpoint().equals("node-c"), "新请求看到 v3");
        System.out.println("版本 " + table.snapshot().version());

        if (List.of(args).contains("--goal")) {
            var source = new HashMap<>(snapshot(4, "node-d").routes());
            var immutable = new Snapshot(4, source);
            source.clear();
            check(immutable.resolve("/orders").isPresent(), "创建快照时拷贝输入");
            check(table.replace(immutable), "发布 v4");
            check(table.replace(new Snapshot(5, Map.of())), "旧路由可被下线");
            check(table.snapshot().resolve("/orders").isEmpty(), "空表是有效发布");
            check(!table.replace(snapshot(5, "node-wrong")), "同版本重复事件忽略");
            raceUpdates(table);
            System.out.println("通过：快照防篡改、路由下线、并发更新");
        }
    }

    private static Snapshot snapshot(long version, String endpoint) {
        return new Snapshot(version, Map.of("/orders", new Destination("OrderService", endpoint)));
    }

    private static void raceUpdates(RouteTable table) throws InterruptedException {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        Thread older = writer(table, snapshot(6, "node-f"), ready, start);
        Thread newer = writer(table, snapshot(7, "node-g"), ready, start);
        older.start();
        newer.start();
        ready.await();
        start.countDown();
        older.join();
        newer.join();
        check(table.snapshot().version() == 7, "任一写入顺序都留下最新版本");
    }

    private static Thread writer(RouteTable table, Snapshot snapshot,
            CountDownLatch ready, CountDownLatch start) {
        return new Thread(() -> {
            ready.countDown();
            try {
                start.await();
                table.replace(snapshot);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
