package cn.shiinapple.amr.missionflow;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import static cn.shiinapple.amr.missionflow.MyMissionFlow.Phase.*;

public final class MissionFlowTest {
    private static final List<String> ROUTE = List.of("A", "B", "C");

    private static MyMissionFlow ready() {
        var flow = new MyMissionFlow();
        flow.editMap(ROUTE);
        flow.publishMap();
        return flow;
    }

    private static MyMissionFlow started() {
        var flow = ready();
        if (!flow.start("job-1", ROUTE)) {
            throw new IllegalStateException("模拟路线启动失败");
        }
        return flow;
    }

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
                throw new AssertionError("进阶目标尚未达成，请编辑 MyMissionFlow.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("发布地图只进入待命状态", IDLE,
                () -> ready().phase());
        check("编辑后还没发布", false,
                () -> {
                    var flow = ready();
                    flow.editMap(List.of("A", "B", "D"));
                    return flow.start("job-1", ROUTE);
                });
        check("地图上无 A→C", false,
                () -> ready().start("job-1", List.of("A", "C")));
        check("页面关闭不改变待应答任务", List.of(false, AWAITING_ACK),
                () -> {
                    var flow = started();
                    flow.closePage();
                    return List.of(flow.pageOpen(), flow.phase());
                });
    }

    private static void goal() {
        check("ACK 后仍要等到站反馈", List.of(RUNNING, 0),
                () -> {
                    var flow = started();
                    flow.acknowledge("job-1");
                    return List.of(flow.phase(), flow.reached());
                });
        check("先收到 C 不能跳过 B", List.of(RUNNING, 0),
                () -> {
                    var flow = started();
                    flow.acknowledge("job-1");
                    flow.arrived("job-1", "C");
                    return List.of(flow.phase(), flow.reached());
                });
        check("收到应答前忽略到站反馈", List.of(AWAITING_ACK, 0), () -> {
            var flow = started();
            flow.arrived("job-1", "C");
            return List.of(flow.phase(), flow.reached());
        });
        check("旧任务反馈不推进本轮，当前任务到 B 后才到 C",
                List.of(0, RUNNING, 1, COMPLETED, 2), () -> {
            var flow = started();
            flow.acknowledge("job-1");
            flow.arrived("old", "B");
            int afterForeignReply = flow.reached();
            flow.arrived("job-1", "B");
            var atB = flow.phase();
            int index = flow.reached();
            flow.arrived("job-1", "C");
            return List.of(afterForeignReply, atB, index, flow.phase(), flow.reached());
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
