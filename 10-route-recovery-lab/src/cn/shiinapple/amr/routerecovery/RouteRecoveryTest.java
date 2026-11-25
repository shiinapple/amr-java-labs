package cn.shiinapple.amr.routerecovery;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import cn.shiinapple.amr.routerecovery.MyRouteRecovery.*;

public final class RouteRecoveryTest {
    private static final List<String> SIMPLE = List.of("A", "B", "C");
    private static final List<String> LOOP = List.of("A", "B", "C", "B", "D");

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
                throw new AssertionError("进阶目标尚未达成，请编辑 MyRouteRecovery.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("应答成功后记送到哪一段", List.of(0, true, 2),
                () -> {
                    var runner = new MyRouteRecovery();
                    var reply = new CompletableFuture<Boolean>();
                    var result = runner.pump(2, () -> reply);
                    int beforeAck = runner.sent();
                    reply.complete(true);
                    return List.of(beforeAck, result.join(), runner.sent());
                });
        check("明确拒绝，已发计数不前进", List.of(false, 0),
                () -> {
                    var runner = new MyRouteRecovery();
                    return List.of(runner.pump(2, () -> CompletableFuture.completedFuture(false)).join(), runner.sent());
                });
        check("简单断点恢复先进入暂停", Optional.of(new Resume(Phase.PAUSED, 1, List.of("B", "C"))),
                () -> new MyRouteRecovery().restore(new Checkpoint(SIMPLE, 1, "B"), SIMPLE));
    }

    private static void goal() {
        check("上一包应答前第二次调度不调用发送", List.of(1, true, false, 2),
                () -> {
                    var runner = new MyRouteRecovery();
                    var sends = new AtomicInteger();
                    var reply = new CompletableFuture<Boolean>();
                    Supplier<CompletableFuture<Boolean>> send = () -> { sends.incrementAndGet(); return reply; };
                    var first = runner.pump(2, send);
                    var duplicate = runner.pump(2, send);
                    reply.complete(true);
                    return List.of(sends.get(), first.join(), duplicate.join(), runner.sent());
                });
        check("发送失败后允许下一次人工触发", List.of(false, true, 2),
                () -> {
                    var runner = new MyRouteRecovery();
                    var failed = runner.pump(2, () -> CompletableFuture.failedFuture(new IllegalStateException("模拟发送失败")));
                    var retried = runner.pump(2, () -> CompletableFuture.completedFuture(true));
                    return List.of(failed.join(), retried.join(), runner.sent());
                });
        check("按下标恢复第二次经过 B", Optional.of(new Resume(Phase.PAUSED, 3, List.of("B", "D"))),
                () -> new MyRouteRecovery().restore(new Checkpoint(LOOP, 3, "B"), LOOP));
        check("路线编辑过，不能套用旧进度", Optional.empty(),
                () -> new MyRouteRecovery().restore(new Checkpoint(LOOP, 3, "B"), List.of("A", "B", "D", "E")));
        check("站点名与保存下标对不上时拒绝恢复", Optional.empty(),
                () -> new MyRouteRecovery().restore(new Checkpoint(SIMPLE, 2, "B"), SIMPLE));
        check("重试时读取已经推进的位置", 2,
                () -> {
                    var current = new AtomicInteger(1);
                    var stored = new AtomicInteger();
                    Runnable retry = new MyRouteRecovery().prepareRetry(current::get, stored::set);
                    current.set(2);
                    retry.run();
                    return stored.get();
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
