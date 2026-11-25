package cn.shiinapple.amr.trafficlookahead;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import cn.shiinapple.amr.trafficlookahead.MyTrafficLookahead.*;

public final class TrafficLookaheadTest {
    private static final MyTrafficLookahead TRAFFIC = new MyTrafficLookahead();
    private static final Point START = new Point(0, 0);
    private static final Point AT = new Point(3, 0);
    private static final Point CORNER = new Point(4, 0);
    private static final Point LAST = new Point(4, 4);
    private static final List<Point> ROUTE = List.of(START, CORNER, LAST);

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
                throw new AssertionError("进阶目标尚未达成，请编辑 MyTrafficLookahead.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("当前段从当前位置切出 1 米", List.of(new Segment(AT, CORNER)),
                () -> TRAFFIC.occupy(ROUTE, 0, AT, 1));
        check("0 米没有占位", List.of(),
                () -> TRAFFIC.occupy(ROUTE, 0, AT, 0));
        check("两车互相等待", Set.of(Set.of("A", "B")),
                () -> TRAFFIC.waitCycles(Map.of("A", List.of("B"), "B", List.of("A"))));
        check("只等图外阻塞方不会成环", Set.of(),
                () -> TRAFFIC.waitCycles(Map.of("A", List.of("external"))));
    }

    private static void goal() {
        check("跨拐角凑满 3 米", List.of(new Segment(AT, CORNER), new Segment(CORNER, new Point(4, 2))),
                () -> TRAFFIC.occupy(ROUTE, 0, AT, 3));
        check("跳过零长度段，距离再大也到路线终点为止", List.of(new Segment(CORNER, LAST)),
                () -> TRAFFIC.occupy(List.of(CORNER, CORNER, LAST), 0, CORNER, 10));
        check("非首要等待原因也能参与三车成环", Set.of(Set.of("A", "B", "C")),
                () -> TRAFFIC.waitCycles(Map.of("A", List.of("external", "B"), "B", List.of("C"), "C", List.of("A"))));
        check("一个大环与另一个小环各自分组", Set.of(Set.of("A", "B", "C"), Set.of("D", "E")),
                () -> TRAFFIC.waitCycles(Map.of("A", List.of("B", "D"), "B", List.of("A", "C"), "C", List.of("A"), "D", List.of("E"), "E", List.of("D"))));
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
