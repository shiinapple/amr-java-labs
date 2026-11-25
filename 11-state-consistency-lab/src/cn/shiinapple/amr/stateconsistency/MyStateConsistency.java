package cn.shiinapple.amr.stateconsistency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

public final class MyStateConsistency {
    record Layout(String name, List<String> paths) {
        Layout {
            paths = List.copyOf(paths);
        }
    }

    record Preview(boolean online, int duplicatePaths) {}

    static final class Topology {
        private int calculations;

        int calculations() { return calculations; }

        Preview check(String chassis, Layout layout, boolean online, boolean displayOnly) {
            // 每次刷新都做拓扑检查，进阶目标是仅缓存展示用的拓扑结果。
            calculations++;
            int distinctPaths = (int) layout.paths().stream().distinct().count();
            return new Preview(online, layout.paths().size() - distinctPaths);
        }
    }

    static final class LiveCollection {
        private final Map<String, String> items = new HashMap<>();
        private long revision;

        long startSnapshot() { return revision; }

        void upsert(String id, String state) {
            revision++;
            items.put(id, state);
        }

        void remove(String id) {
            revision++;
            items.remove(id);
        }

        void merge(long startedAt, Map<String, String> snapshot) {
            // 起步实现用快照直接替换整份列表。
            items.clear();
            items.putAll(snapshot);
        }

        Map<String, String> items() { return Map.copyOf(items); }
    }

    record SyncPreview(int localVersion, int remoteVersion) {}

    static final class Confirmation {
        private int localVersion;

        Confirmation(int localVersion) {
            this.localVersion = localVersion;
        }

        void edit() { localVersion++; }

        SyncPreview preview(int remoteVersion) { return new SyncPreview(localVersion, remoteVersion); }

        boolean confirm(SyncPreview preview, IntSupplier readRemoteVersion) {
            if (preview.localVersion() != localVersion) {
                return false;
            }
            // 模拟外部读取时，本地仍可能有新的编辑。
            return readRemoteVersion.getAsInt() == preview.remoteVersion();
        }
    }
}
