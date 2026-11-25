package cn.shiinapple.amr.minhoppath;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MyPathPlanner {
    record Edge(String id, String from, String to, long meters) {}
    record Route(List<String> stations, List<String> edges, long meters) {}

    Optional<Route> find(List<Edge> edges, String start, String target) {
        Map<String, List<Edge>> graph = new HashMap<>();
        for (Edge edge : edges) {
            graph.computeIfAbsent(edge.from(), key -> new ArrayList<>()).add(edge);
        }

        // 广度优先：第一次见到一个站点就记下前驱。
        var queue = new ArrayDeque<String>();
        Map<String, Edge> previous = new HashMap<>();
        previous.put(start, null);
        queue.add(start);
        while (!queue.isEmpty()) {
            String station = queue.remove();
            if (station.equals(target)) {
                return Optional.of(buildRoute(start, target, previous));
            }
            for (Edge next : graph.getOrDefault(station, List.of())) {
                if (!previous.containsKey(next.to())) {
                    previous.put(next.to(), next);
                    queue.add(next.to());
                }
            }
        }
        return Optional.empty();
    }

    private Route buildRoute(String start, String target, Map<String, Edge> previous) {
        var stations = new ArrayDeque<String>();
        var edgeIds = new ArrayDeque<String>();
        long meters = 0;
        String station = target;
        stations.addFirst(station);
        while (!station.equals(start)) {
            Edge edge = previous.get(station);
            edgeIds.addFirst(edge.id());
            meters += edge.meters();
            station = edge.from();
            stations.addFirst(station);
        }
        return new Route(List.copyOf(stations), List.copyOf(edgeIds), meters);
    }
}
