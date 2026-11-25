package cn.shiinapple.amr.trafficrules;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import cn.shiinapple.amr.trafficrules.MyTrafficRules.Vehicle;
import static cn.shiinapple.amr.trafficrules.MyTrafficRules.Reason.*;

public final class TrafficRulesTest {
    private static final MyTrafficRules RULES = new MyTrafficRules();
    private static final Vehicle A = new Vehicle("A", true, false, false);
    private static final Vehicle B_RETURNING = new Vehicle("B", true, true, false);
    private static final Vehicle B_BLOCKED = new Vehicle("B", true, true, true);

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
                throw new AssertionError("进阶目标尚未达成，请编辑 MyTrafficRules.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("A 占有起点，普通请求方等待", List.of(RESOURCE_OCCUPIED),
                () -> RULES.decide("A", List.of(A, new Vehicle("B", true, false, false))).get("B"));
        check("起点空闲且 B 可返航，A 让行", Map.of("A", List.of(RETURN_YIELD), "B", List.of()),
                () -> RULES.decide(null, List.of(A, B_RETURNING)));
        check("障碍和占用原因都要保留", List.of(OBSTACLE, RESOURCE_OCCUPIED),
                () -> RULES.decide("A", List.of(A, B_BLOCKED)).get("B"));
    }

    private static void goal() {
        check("返航车被 A 占住，A 可以先离开", Map.of("A", List.of(), "B", List.of(RESOURCE_OCCUPIED)),
                () -> RULES.decide("A", List.of(A, B_RETURNING)));
        check("有障碍的返航车不要求其他车等待", Map.of("A", List.of(), "B", List.of(OBSTACLE)),
                () -> RULES.decide(null, List.of(A, B_BLOCKED)));
        check("一个返航候选有障碍时仍能选下一位", Map.of("A", List.of(RETURN_YIELD), "B", List.of(OBSTACLE, RETURN_YIELD), "C", List.of()),
                () -> RULES.decide(null, List.of(A, B_BLOCKED, new Vehicle("C", true, true, false))));
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
