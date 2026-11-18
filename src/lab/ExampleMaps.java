package lab;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** 测试和界面共用的离线地图，不连接真实车辆或 Gateway。 */
public final class ExampleMaps {
    private ExampleMaps() {}

    public static List<MapPath> teachingExample() {
        return List.of(
                line("A", 4, 0, 6, 0), line("B", 0, 0, 5, 0),
                line("C", 2, 3, 4, 3), line("D", 10, 0, 11, 0),
                line("E", 4, 0.02, 5, 0.02)
        );
    }

    /** 彼此分散的小组：每两条线间隔 2cm，其余组相距较远。 */
    public static List<MapPath> sparse(int count) {
        List<MapPath> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int group = i / 2;
            double x = (group % 25) * 5.0;
            double y = (group / 25) * 5.0 + (i % 2) * 0.02;
            result.add(line("P" + i, x, y, x + 2, y));
        }
        return result;
    }

    /** 固定种子，包含交叉、短线和不同方向；每次可以重现相同地图。 */
    public static List<MapPath> random(int count, long seed) {
        Random random = new Random(seed);
        List<MapPath> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double x = random.nextInt(40), y = random.nextInt(40);
            double dx = random.nextInt(9) - 4, dy = random.nextInt(9) - 4;
            result.add(line("R" + i, x, y, x + dx, y + dy));
        }
        return result;
    }

    public static MapPath line(String id, double x1, double y1, double x2, double y2) {
        return new MapPath(id, new Point(x1, y1), new Point(x2, y2));
    }
}
