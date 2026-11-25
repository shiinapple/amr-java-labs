package cn.shiinapple.amr.routebatches;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class MyRouteBatches {
    record Leg(String from, String to) {}

    List<Leg> compile(List<String> stations) {
        List<Leg> legs = new ArrayList<>();
        for (int i = 1; i < stations.size(); i++) {
            legs.add(new Leg(stations.get(i - 1), stations.get(i)));
        }
        return List.copyOf(legs);
    }

    List<Leg> nextPacket(List<Leg> legs, int sent, int done,
                         int nextActionAt, int blockedLeg, Set<Integer> turningStations) {
        if (sent != done) {
            return List.of();
        }

        // 默认取两段。进阶任务再把动作、通道、转弯输入用起来。
        int end = Math.min(sent + 2, legs.size());
        return List.copyOf(legs.subList(sent, end));
    }
}
