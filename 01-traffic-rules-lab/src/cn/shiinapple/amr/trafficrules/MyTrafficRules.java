package cn.shiinapple.amr.trafficrules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MyTrafficRules {
    enum Reason { RESOURCE_OCCUPIED, OBSTACLE, RETURN_YIELD }

    record Vehicle(String id, boolean requestsDock, boolean returning, boolean obstacle) {}

    Map<String, List<Reason>> decide(String dockOwner, List<Vehicle> vehicles) {
        Map<String, List<Reason>> waits = new LinkedHashMap<>();
        for (Vehicle vehicle : vehicles) {
            List<Reason> reasons = new ArrayList<>();
            if (vehicle.obstacle()) {
                reasons.add(Reason.OBSTACLE);
            }
            if (vehicle.requestsDock() && dockOwner != null && !dockOwner.equals(vehicle.id())) {
                reasons.add(Reason.RESOURCE_OCCUPIED);
            }
            waits.put(vehicle.id(), reasons);
        }

        // 起步实现只看返航标记。想一想该候选车自己还能继续吗？
        Vehicle priority = vehicles.stream()
                .filter(vehicle -> vehicle.requestsDock() && vehicle.returning())
                .findFirst()
                .orElse(null);
        if (priority != null) {
            for (Vehicle vehicle : vehicles) {
                if (vehicle.requestsDock() && !vehicle.id().equals(priority.id())) {
                    waits.get(vehicle.id()).add(Reason.RETURN_YIELD);
                }
            }
        }
        waits.replaceAll((id, reasons) -> List.copyOf(reasons));
        return Map.copyOf(waits);
    }
}
