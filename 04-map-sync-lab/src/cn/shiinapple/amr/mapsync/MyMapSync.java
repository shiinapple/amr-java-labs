package cn.shiinapple.amr.mapsync;

import java.util.List;
import java.util.Objects;

public final class MyMapSync {
    enum Action { NONE, ADD, UPDATE, KEEP_LOCAL, CONFLICT, DELETE, BLOCKED }

    record Baseline(String local, String remote) {}
    record Proposal(Action action, List<String> references) {}

    Proposal propose(Baseline previous, String local, String remote,
                     boolean unambiguous, List<String> references) {
        // 起步实现假设已找对站点，并把两端旧基线当作同一个值。
        if (Objects.equals(local, remote)) {
            return result(Action.NONE);
        }
        if (previous == null) {
            return result(local == null ? Action.ADD : Action.CONFLICT);
        }
        if (Objects.equals(previous.remote(), remote)) {
            return result(Action.KEEP_LOCAL);
        }
        if (Objects.equals(previous.local(), local)) {
            return result(Action.UPDATE);
        }
        return result(Action.CONFLICT);
    }

    private Proposal result(Action action) {
        return new Proposal(action, List.of());
    }
}
