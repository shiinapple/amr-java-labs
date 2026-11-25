package cn.shiinapple.amr.missionflow;

import java.util.List;

public final class MyMissionFlow {
    enum Phase { IDLE, AWAITING_ACK, RUNNING, COMPLETED }

    private List<String> localMap = List.of();
    private List<String> publishedMap = List.of();
    private List<String> route = List.of();
    private Phase phase = Phase.IDLE;
    private String missionId;
    private int reached;
    private boolean pageOpen = true;

    void editMap(List<String> stations) {
        localMap = List.copyOf(stations);
    }

    void publishMap() {
        publishedMap = localMap;
    }

    boolean start(String id, List<String> requestedRoute) {
        if (phase == Phase.AWAITING_ACK || phase == Phase.RUNNING
                || requestedRoute.size() < 2 || !localMap.equals(publishedMap)) {
            return false;
        }
        for (int i = 1; i < requestedRoute.size(); i++) {
            int from = publishedMap.indexOf(requestedRoute.get(i - 1));
            int to = publishedMap.indexOf(requestedRoute.get(i));
            if (from < 0 || to != from + 1) {
                return false;
            }
        }
        missionId = id;
        route = List.copyOf(requestedRoute);
        reached = 0;
        phase = Phase.AWAITING_ACK;
        return true;
    }

    void acknowledge(String id) {
        if (phase == Phase.AWAITING_ACK && id.equals(missionId)) {
            // 起步实现把控制器应答直接当成整条路线完成。
            phase = Phase.COMPLETED;
        }
    }

    void arrived(String id, String station) {
        if (id.equals(missionId) && route.get(route.size() - 1).equals(station)) {
            reached = route.size() - 1;
            phase = Phase.COMPLETED;
        }
    }

    void closePage() {
        pageOpen = false;
    }

    boolean pageOpen() { return pageOpen; }
    Phase phase() { return phase; }
    int reached() { return reached; }
}
