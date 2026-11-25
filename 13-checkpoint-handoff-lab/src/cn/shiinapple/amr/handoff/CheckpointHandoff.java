package cn.shiinapple.amr.handoff;

import java.util.List;
import java.util.Objects;

/** 单个任务的交接状态。输入为教学用的站序和到站信号。 */
final class CheckpointHandoff {
    enum Phase { WAITING, RETURNING, READY }

    record Route(String version, List<String> stations) {
        Route {
            Objects.requireNonNull(version);
            stations = List.copyOf(stations);
            if (stations.size() < 2) {
                throw new IllegalArgumentException("路线至少需要两个站");
            }
        }
    }

    record Checkpoint(String routeVersion, int stationIndex, String station) {}
    record Arrival(String attemptId, String station, double distanceMeters, boolean finished) {}

    private final Route route;
    private final int checkpointIndex;
    private final double toleranceMeters;
    private Phase phase = Phase.WAITING;
    private String activeAttempt;

    CheckpointHandoff(Route route, Checkpoint checkpoint, double toleranceMeters) {
        if (!Double.isFinite(toleranceMeters) || toleranceMeters < 0) {
            throw new IllegalArgumentException("距离容差必须为有限非负值");
        }

        int index = checkpoint.stationIndex();
        // 同名站可以重复出现，断点通过路线版本、下标和名称共同校验。
        if (!route.version().equals(checkpoint.routeVersion())
                || index < 0 || index >= route.stations().size() - 1
                || !route.stations().get(index).equals(checkpoint.station())) {
            throw new IllegalArgumentException("断点与路线不匹配，或没有剩余路段");
        }
        this.route = route;
        this.checkpointIndex = index;
        this.toleranceMeters = toleranceMeters;
    }

    boolean startReturn(String attemptId) {
        Objects.requireNonNull(attemptId);
        if (phase != Phase.WAITING || attemptId.isBlank()) {
            return false;
        }
        activeAttempt = attemptId;
        phase = Phase.RETURNING;
        return true;
    }

    boolean acceptArrival(Arrival arrival) {
        // 回站阶段只改变交接状态，原任务的完成下标始终保留。
        if (phase != Phase.RETURNING || !activeAttempt.equals(arrival.attemptId())
                || !arrival.finished()
                || !route.stations().get(checkpointIndex).equals(arrival.station())
                || !Double.isFinite(arrival.distanceMeters())
                || arrival.distanceMeters() < 0 || arrival.distanceMeters() > toleranceMeters) {
            return false;
        }
        phase = Phase.READY;
        return true;
    }

    Phase phase() {
        return phase;
    }

    List<String> remainingStations() {
        if (phase != Phase.READY) {
            throw new IllegalStateException("到站交接尚未完成");
        }
        // 保留断点站作为第一条未完成路段的起点。
        return route.stations().subList(checkpointIndex, route.stations().size());
    }
}
