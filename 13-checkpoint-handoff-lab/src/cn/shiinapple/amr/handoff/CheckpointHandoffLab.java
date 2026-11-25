package cn.shiinapple.amr.handoff;

import java.util.List;

public final class CheckpointHandoffLab {
    public static void main(String[] args) {
        var route = new CheckpointHandoff.Route("v1", List.of("A", "B", "C", "B", "D"));
        var point = new CheckpointHandoff.Checkpoint("v1", 3, "B");
        var handoff = new CheckpointHandoff(route, point, 0.30);

        check(handoff.phase() == CheckpointHandoff.Phase.WAITING, "初始等待");
        check(handoff.startReturn("return-2"), "开始回站");
        check(!handoff.acceptArrival(arrival("return-2", 0.80)), "同站名但距离过远");
        check(handoff.phase() == CheckpointHandoff.Phase.RETURNING, "拒绝后仍在回站");
        check(handoff.acceptArrival(arrival("return-2", 0.18)), "确认到达第二个 B");
        check(handoff.remainingStations().equals(List.of("B", "D")), "从 B → D 继续");
        System.out.println("续扫站序 " + handoff.remainingStations());

        if (List.of(args).contains("--goal")) {
            var retry = new CheckpointHandoff(route, point, 0.30);
            check(retry.startReturn("return-3"), "开始本次回站");
            check(!retry.acceptArrival(arrival("return-2", 0.18)), "拒绝上一次回站的反馈");
            check(!retry.acceptArrival(arrival("return-3", Double.NaN)), "拒绝无效距离");
            check(!retry.acceptArrival(new CheckpointHandoff.Arrival("return-3", "B", 0.18, false)),
                    "移动完成信号是交接条件");
            check(retry.acceptArrival(arrival("return-3", 0.30)), "等于容差时可交接");
            check(!retry.acceptArrival(arrival("return-3", 0.18)), "同一条反馈不会重复交接");
            try {
                new CheckpointHandoff(route, new CheckpointHandoff.Checkpoint("v0", 3, "B"), 0.30);
                throw new AssertionError("旧地图版本应被拒绝");
            } catch (IllegalArgumentException expected) {
                System.out.println("通过：旧地图、迟到反馈、距离阈值和重复反馈");
            }
        }
    }

    private static CheckpointHandoff.Arrival arrival(String attemptId, double distance) {
        return new CheckpointHandoff.Arrival(attemptId, "B", distance, true);
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
