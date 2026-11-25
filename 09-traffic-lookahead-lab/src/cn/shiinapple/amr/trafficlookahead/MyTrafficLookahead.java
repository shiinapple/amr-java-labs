package cn.shiinapple.amr.trafficlookahead;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MyTrafficLookahead {
    record Point(double x, double y) {}
    record Segment(Point start, Point end) {}

    List<Segment> occupy(List<Point> route, int currentLeg, Point position, double meters) {
        // 起步实现只沿当前路段走，剩余距离没有继续分配。
        Point end = route.get(currentLeg + 1);
        double length = Math.hypot(end.x() - position.x(), end.y() - position.y());
        if (length == 0 || meters <= 0) {
            return List.of();
        }
        if (meters >= length) {
            return List.of(new Segment(position, end));
        }
        double ratio = meters / length;
        Point cut = new Point(position.x() + ratio * (end.x() - position.x()),
                position.y() + ratio * (end.y() - position.y()));
        return List.of(new Segment(position, cut));
    }

    Set<Set<String>> waitCycles(Map<String, List<String>> waits) {
        // 起步实现只检查首要原因造成的两车互等。
        Set<Set<String>> cycles = new HashSet<>();
        waits.forEach((vehicle, blockers) -> {
            if (blockers.isEmpty()) {
                return;
            }
            String first = blockers.get(0);
            List<String> reverse = waits.getOrDefault(first, List.of());
            if (!vehicle.equals(first) && !reverse.isEmpty() && reverse.get(0).equals(vehicle)) {
                cycles.add(Set.of(vehicle, first));
            }
        });
        return Set.copyOf(cycles);
    }
}
