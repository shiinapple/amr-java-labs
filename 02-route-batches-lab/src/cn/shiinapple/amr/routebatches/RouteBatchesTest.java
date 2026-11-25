package cn.shiinapple.amr.routebatches;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import cn.shiinapple.amr.routebatches.MyRouteBatches.Leg;

public final class RouteBatchesTest {
    private static final MyRouteBatches ROUTE = new MyRouteBatches();
    private static final Leg AB = new Leg("A", "B");
    private static final Leg BC = new Leg("B", "C");
    private static final Leg CD = new Leg("C", "D");
    private static final Leg DE = new Leg("D", "E");
    private static final List<Leg> LEGS = ROUTE.compile(List.of("A", "B", "C", "D", "E"));
    private static final int NONE = Integer.MAX_VALUE;

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
                throw new AssertionError("进阶目标尚未达成，请编辑 MyRouteBatches.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("四个相邻路段", List.of(AB, BC, CD, DE),
                () -> LEGS);
        check("首包两段", List.of(AB, BC),
                () -> ROUTE.nextPacket(LEGS, 0, 0, NONE, NONE, Set.of()));
        check("发过两段但只走完一段", List.of(),
                () -> ROUTE.nextPacket(LEGS, 2, 1, NONE, NONE, Set.of()));
        check("第一包走完后取下一包", List.of(CD, DE),
                () -> ROUTE.nextPacket(LEGS, 2, 2, NONE, NONE, Set.of()));
    }

    private static void goal() {
        check("到 B 前截断，等待 B 的动作", List.of(AB),
                () -> ROUTE.nextPacket(LEGS, 0, 0, 1, NONE, Set.of()));
        check("当前站有未处理动作，先不发", List.of(),
                () -> ROUTE.nextPacket(LEGS, 1, 1, 1, NONE, Set.of()));
        check("B→C 是未放行的入口段", List.of(AB),
                () -> ROUTE.nextPacket(LEGS, 0, 0, NONE, 1, Set.of()));
        check("默认包尾 C 是转弯站，带上 C→D", List.of(AB, BC, CD),
                () -> ROUTE.nextPacket(LEGS, 0, 0, NONE, NONE, Set.of(2)));
        check("通道限制不能往后并段，退到 B", List.of(AB),
                () -> ROUTE.nextPacket(LEGS, 0, 0, NONE, 2, Set.of(2)));
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
