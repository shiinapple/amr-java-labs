package cn.shiinapple.amr.batchdelete;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class MyBatchDelete {
    record Station(String id, String label) {}
    record Path(String id, String from, String to) {}
    record Batch(Set<String> stations, Set<String> paths) {}

    private final Map<String, Station> stations = new HashMap<>();
    private final Map<String, Path> paths = new HashMap<>();
    private CompletableFuture<Void> tail = CompletableFuture.completedFuture(null);

    MyBatchDelete(List<Station> stations, List<Path> paths) {
        stations.forEach(this::putStation);
        paths.forEach(path -> this.paths.put(path.id(), path));
    }

    void putStation(Station station) { stations.put(station.id(), station); }

    Map<String, Station> stations() { return Map.copyOf(stations); }
    Map<String, Path> paths() { return Map.copyOf(paths); }

    CompletableFuture<Void> delete(Set<String> selectedStations, Set<String> selectedPaths,
                                   Function<Batch, CompletableFuture<Void>> send) {
        Set<String> pathIds = new HashSet<>(selectedPaths);
        for (Path path : paths.values()) {
            if (selectedStations.contains(path.from()) || selectedStations.contains(path.to())) {
                pathIds.add(path.id());
            }
        }
        Batch batch = new Batch(Set.copyOf(selectedStations), Set.copyOf(pathIds));
        if (batch.stations().isEmpty() && batch.paths().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        Map<String, Station> oldStations = Map.copyOf(stations);
        Map<String, Path> oldPaths = Map.copyOf(paths);
        batch.paths().forEach(paths::remove);
        batch.stations().forEach(stations::remove);

        var operation = tail.thenCompose(ignored -> send.apply(batch)).whenComplete((ignored, error) -> {
            if (error != null) {
                // 起步实现成批覆盖，观察等待期间新增的站点会怎样。
                stations.clear();
                stations.putAll(oldStations);
                paths.clear();
                paths.putAll(oldPaths);
            }
        });
        tail = operation.handle((ignored, error) -> null);
        return operation;
    }
}
