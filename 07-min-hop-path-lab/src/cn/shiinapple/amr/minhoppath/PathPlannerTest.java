package cn.shiinapple.amr.minhoppath;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import cn.shiinapple.amr.minhoppath.MyPathPlanner.*;

public final class PathPlannerTest {
    private static final MyPathPlanner PLANNER = new MyPathPlanner();
    private static final Edge AB = new Edge("ab", "A", "B", 2);
    private static final Edge BD = new Edge("bd", "B", "D", 2);
    private static final Edge AC = new Edge("ac", "A", "C", 3);
    private static final Edge CD = new Edge("cd", "C", "D", 2);
    private static final Route VIA_B = new Route(List.of("A", "B", "D"), List.of("ab", "bd"), 4);

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        baseline();
        if (failed > 0) {
            throw new AssertionError("请先通过基础用例。");
        }
        System.out.printf("基础测试通过 %d 项。%n", passed);
        if (Arrays.asList(args).contains("--goal")) {
            int basicPassed = passed;
            goal();
            System.out.printf("进阶测试通过 %d 项，未通过 %d 项。%n", passed - basicPassed, failed);
            if (failed > 0) {
                throw new AssertionError("进阶目标尚未达成，请编辑 MyPathPlanner.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("一条边优先于两条更短的边", Optional.of(new Route(List.of("A", "D"), List.of("ad"), 10)),
                () -> PLANNER.find(List.of(AB, BD, new Edge("ad", "A", "D", 10)), "A", "D"));
        check("单向边不能反着走", Optional.empty(),
                () -> PLANNER.find(List.of(AB, BD), "D", "A"));
        check("出发点就是目标点", Optional.of(new Route(List.of("A"), List.of(), 0)),
                () -> PLANNER.find(List.of(), "A", "A"));
        check("找回前驱、边 ID 和累计距离", Optional.of(VIA_B),
                () -> PLANNER.find(List.of(AB, BD), "A", "D"));
    }

    private static void goal() {
        check("B 路线更短，即使先录入 C 的边", Optional.of(VIA_B),
                () -> PLANNER.find(List.of(AC, CD, AB, BD), "A", "D"));
        check("同一对站点的两条平行边择短", Optional.of(new Route(List.of("A", "B"), List.of("ab"), 2)),
                () -> PLANNER.find(List.of(new Edge("slow", "A", "B", 20), AB), "A", "B"));
        check("已入队的较长代价要被更优前驱替换", Optional.of(new Route(List.of("A", "C", "D", "E"), List.of("ac", "cd", "de"), 6)),
                () -> PLANNER.find(List.of(AB, new Edge("long", "B", "D", 100), AC, CD, new Edge("de", "D", "E", 1)), "A", "E"));
    }

    private static void check(String name, Object expected, Supplier<?> exercise) {
        try {
            Object actual = exercise.get();
            if (Objects.equals(expected, actual)) {
                passed++;
                System.out.printf("[通过] %s → %s%n", name, actual);
            } else {
                failed++;
                System.err.printf("[待通过] %s，预期 %s，实际 %s%n", name, expected, actual);
            }
        } catch (RuntimeException error) {
            failed++;
            System.err.printf("[待通过] %s，运行出错 %s%n", name, error);
        }
    }
}
