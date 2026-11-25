package cn.shiinapple.amr.codetracing;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import cn.shiinapple.amr.codetracing.MyCallTrace.*;

public final class CallTraceTest {
    private static final MyCallTrace TRACER = new MyCallTrace();
    private static final Event START = new Event("r1", "browser", "gateway.start", "route=A→B", "accepted", false);
    private static final Event HOLD = new Event("r1", "gateway", "traffic.decide", "owner=B", "hold", true);
    private static final Event SHOW = new Event("r1", "gateway", "browser.status", "hold", "displayed", false);
    private static final Event RETRY = new Event("r1", "gateway", "controller.send", "A→B", "timeout", true);
    private static final Event OTHER = new Event("r2", "gateway", "controller.send", "X→Y", "ack", false);

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
                throw new AssertionError("进阶目标尚未达成，请编辑 MyCallTrace.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("从页面调用追到交通规则", new Report(List.of(START, HOLD), Optional.of(HOLD)),
                () -> TRACER.inspect(List.of(START, HOLD), "r1"));
        check("无报错的简单轨迹", new Report(List.of(START), Optional.empty()),
                () -> TRACER.inspect(List.of(START), "r1"));
        check("空日志", new Report(List.of(), Optional.empty()),
                () -> TRACER.inspect(List.of(), "r1"));
    }

    private static void goal() {
        check("交错日志只读当前请求", new Report(List.of(START, HOLD, SHOW), Optional.of(HOLD)),
                () -> TRACER.inspect(List.of(START, OTHER, HOLD, SHOW), "r1"));
        check("找到第一处失败而不是最后一处", Optional.of(HOLD),
                () -> TRACER.inspect(List.of(START, HOLD, RETRY), "r1").firstFailure());
        check("找不到请求不给出另一任务的轨迹", new Report(List.of(), Optional.empty()),
                () -> TRACER.inspect(List.of(START, OTHER), "missing"));
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
