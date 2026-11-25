package cn.shiinapple.gateway.routing;

import java.util.Map;
import java.util.Optional;

/** 注册侧提交全量快照，调用侧可以在一条请求内复用同一个快照。 */
final class RouteTable {
    record Destination(String service, String endpoint) {
        Destination {
            if (service.isBlank() || endpoint.isBlank()) {
                throw new IllegalArgumentException("服务名和地址不能为空");
            }
        }
    }

    record Snapshot(long version, Map<String, Destination> routes) {
        Snapshot {
            if (version < 0) throw new IllegalArgumentException("版本必须非负");
            // 每个值也是不可变的 record，因此读者拿到的整张表不可变。
            routes = Map.copyOf(routes);
        }

        Optional<Destination> resolve(String path) {
            return Optional.ofNullable(routes.get(path));
        }
    }

    private volatile Snapshot current = new Snapshot(0, Map.of());

    synchronized boolean replace(Snapshot incoming) {
        // 加锁覆盖“比较版本 + 更新”，否则两个写线程可能先后覆盖掉新版本。
        if (incoming.version() <= current.version()) {
            return false;
        }
        current = incoming;
        return true;
    }

    Snapshot snapshot() {
        return current;
    }
}
