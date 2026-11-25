package cn.shiinapple.amr.batchdelete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import cn.shiinapple.amr.batchdelete.MyBatchDelete.*;

public final class BatchDeleteTest {
    private static final Station A = new Station("A", "A");
    private static final Station B = new Station("B", "B");
    private static final Station C = new Station("C", "C");
    private static final Station D = new Station("D", "D");
    private static final Path AB = new Path("ab", "A", "B");
    private static final Path BC = new Path("bc", "B", "C");

    private static MyBatchDelete map() {
        return new MyBatchDelete(List.of(A, B, C, D), List.of(AB, BC));
    }

    private static void reject(CompletableFuture<Void> response) {
        response.completeExceptionally(new IllegalStateException("模拟服务端拒绝"));
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
                throw new AssertionError("进阶目标尚未达成，请编辑 MyBatchDelete.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("删除 B 同批处理两条关联路径", List.of(Set.of("A", "C", "D"), Set.of(), List.of(new Batch(Set.of("B"), Set.of("ab", "bc")))),
                () -> {
                    var map = map();
                    var requests = new ArrayList<Batch>();
                    var reply = new CompletableFuture<Void>();
                    var operation = map.delete(Set.of("B"), Set.of(), batch -> { requests.add(batch); return reply; });
                    var visibleWhilePending = map.stations().keySet();
                    reply.complete(null);
                    operation.join();
                    return List.of(visibleWhilePending, map.paths().keySet(), requests);
                });
        check("单独删除路径不改变站点", List.of(Set.of("A", "B", "C", "D"), Set.of("bc")),
                () -> {
                    var map = map();
                    map.delete(Set.of(), Set.of("ab"), batch -> CompletableFuture.completedFuture(null)).join();
                    return List.of(map.stations().keySet(), map.paths().keySet());
                });
        check("服务端拒绝后该批失败并恢复 B 和路径", List.of(true, Set.of("A", "B", "C", "D"), Set.of("ab", "bc")),
                () -> {
                    var map = map();
                    var reply = new CompletableFuture<Void>();
                    var operation = map.delete(Set.of("B"), Set.of(), batch -> reply);
                    reject(reply);
                    return List.of(operation.isCompletedExceptionally(), map.stations().keySet(), map.paths().keySet());
                });
    }

    private static void goal() {
        check("回滚保留后来新增的 E，以及同 ID 的新版 B", List.of(Set.of("A", "B", "C", "D", "E"), new Station("B", "新版 B"), Set.of("ab", "bc")),
                () -> {
                    var map = map();
                    var reply = new CompletableFuture<Void>();
                    map.delete(Set.of("B"), Set.of(), batch -> reply);
                    map.putStation(new Station("E", "E"));
                    map.putStation(new Station("B", "新版 B"));
                    reject(reply);
                    return List.of(map.stations().keySet(), map.stations().get("B"), map.paths().keySet());
                });
        check("重复提交同一站点及路径只发送一次", List.of(true, List.of(new Batch(Set.of("B"), Set.of("ab", "bc")))),
                () -> {
                    var map = map();
                    var requests = new ArrayList<Batch>();
                    var reply = new CompletableFuture<Void>();
                    map.delete(Set.of("B"), Set.of(), batch -> { requests.add(batch); return reply; });
                    var duplicate = map.delete(Set.of("B"), Set.of("ab"), batch -> {
                        requests.add(batch);
                        return CompletableFuture.completedFuture(null);
                    });
                    boolean noNeedToWait = duplicate.isDone();
                    reply.complete(null);
                    return List.of(noNeedToWait, requests);
                });
        check("第一批失败，第二批只提交新选中的 D 并保留该次删除", List.of(1, true, List.of(Set.of("B"), Set.of("D")), Set.of("A", "B", "C")),
                () -> {
                    var map = map();
                    var requests = new ArrayList<Set<String>>();
                    var reply = new CompletableFuture<Void>();
                    var first = map.delete(Set.of("B"), Set.of(), batch -> { requests.add(batch.stations()); return reply; });
                    var second = map.delete(Set.of("B", "D"), Set.of(), batch -> {
                        requests.add(batch.stations());
                        return CompletableFuture.completedFuture(null);
                    });
                    int sentBeforeRejection = requests.size();
                    reject(reply);
                    second.join();
                    return List.of(sentBeforeRejection, first.isCompletedExceptionally(), requests, map.stations().keySet());
                });
        check("失败后可以针对相同 ID 再次提交", List.of(2, Set.of("A", "C", "D")),
                () -> {
                    var map = map();
                    var requests = new ArrayList<Batch>();
                    var reply = new CompletableFuture<Void>();
                    map.delete(Set.of("B"), Set.of(), batch -> { requests.add(batch); return reply; });
                    reject(reply);
                    map.delete(Set.of("B"), Set.of(), batch -> {
                        requests.add(batch);
                        return CompletableFuture.completedFuture(null);
                    }).join();
                    return List.of(requests.size(), map.stations().keySet());
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
