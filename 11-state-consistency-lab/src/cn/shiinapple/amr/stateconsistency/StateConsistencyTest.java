package cn.shiinapple.amr.stateconsistency;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import cn.shiinapple.amr.stateconsistency.MyStateConsistency.*;

public final class StateConsistencyTest {
    private static final Layout MAP = new Layout("M", List.of("ab", "ab"));
    private static final Layout UPDATED = new Layout("M", List.of("ab", "bc"));

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
                throw new AssertionError("进阶目标尚未达成，请编辑 MyStateConsistency.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("基础预检能计算重复路径", new Preview(true, 1),
                () -> new Topology().check("cleaner", MAP, true, true));
        check("没有新事件时快照可以移除缺失对象", Map.of("A", "at-Y"),
                () -> {
                    var state = new LiveCollection();
                    state.upsert("A", "at-X");
                    state.upsert("old", "at-X");
                    state.merge(state.startSnapshot(), Map.of("A", "at-Y"));
                    return state.items();
                });
        check("本地和远端版本均一致，可确认", true,
                () -> {
                    var op = new Confirmation(1);
                    return op.confirm(op.preview(5), () -> 5);
                });
        check("远端变更则要求重新预览", false,
                () -> {
                    var op = new Confirmation(1);
                    return op.confirm(op.preview(5), () -> 6);
                });
    }

    private static void goal() {
        check("在线状态变了也能复用拓扑计算", List.of(new Preview(true, 1), new Preview(false, 1), 1),
                () -> {
                    var topology = new Topology();
                    var first = topology.check("cleaner", MAP, true, true);
                    var second = topology.check("cleaner", MAP, false, true);
                    return List.of(first, second, topology.calculations());
                });
        check("同名地图内容变化后重算，启动每次独立重算", List.of(new Preview(true, 0), 4),
                () -> {
                    var topology = new Topology();
                    topology.check("cleaner", MAP, true, true);
                    topology.check("cleaner", MAP, true, true);
                    var changed = topology.check("cleaner", UPDATED, true, true);
                    topology.check("cleaner", UPDATED, true, false);
                    topology.check("cleaner", UPDATED, true, false);
                    return List.of(changed, topology.calculations());
                });
        check("不同车型各算一次", 2,
                () -> {
                    var topology = new Topology();
                    topology.check("cleaner", MAP, true, true);
                    topology.check("pusher", MAP, true, true);
                    topology.check("cleaner", MAP, true, true);
                    return topology.calculations();
                });
        check("旧快照保住新位置、新对象和删除，仍可更新未变化的项", Map.of("A", "at-B", "NEW", "at-D", "U", "fresh"),
                () -> {
                    var state = new LiveCollection();
                    state.upsert("A", "at-A");
                    state.upsert("OLD", "at-C");
                    state.upsert("U", "old");
                    long request = state.startSnapshot();
                    state.remove("OLD");
                    state.upsert("A", "at-B");
                    state.upsert("NEW", "at-D");
                    state.merge(request, Map.of("A", "at-A", "OLD", "at-C", "U", "fresh"));
                    return state.items();
                });
        check("请求后更新的现有对象在快照缺失时仍保留", Map.of("A", "at-B"),
                () -> {
                    var state = new LiveCollection();
                    state.upsert("A", "at-A");
                    long request = state.startSnapshot();
                    state.upsert("A", "at-B");
                    state.merge(request, Map.of());
                    return state.items();
                });
        check("读取远端期间的本地编辑也会使预览失效", false,
                () -> {
                    var op = new Confirmation(1);
                    var preview = op.preview(5);
                    return op.confirm(preview, () -> { op.edit(); return 5; });
                });
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
