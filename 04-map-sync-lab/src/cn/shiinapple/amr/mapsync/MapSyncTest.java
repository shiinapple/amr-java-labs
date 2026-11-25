package cn.shiinapple.amr.mapsync;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import cn.shiinapple.amr.mapsync.MyMapSync.*;
import static cn.shiinapple.amr.mapsync.MyMapSync.Action.*;

public final class MapSyncTest {
    private static final MyMapSync SYNC = new MyMapSync();
    private static final Baseline AGREED = new Baseline("V1", "V1");

    private static Action action(Baseline base, String local, String remote) {
        return SYNC.propose(base, local, remote, true, List.of()).action();
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
                throw new AssertionError("进阶目标尚未达成，请编辑 MyMapSync.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("只改本地", KEEP_LOCAL,
                () -> action(AGREED, "V2", "V1"));
        check("只改远端", UPDATE,
                () -> action(AGREED, "V1", "V2"));
        check("双方改成不同内容", CONFLICT,
                () -> action(AGREED, "V2", "V3"));
        check("双方现在已经一致", NONE,
                () -> action(AGREED, "V2", "V2"));
        check("远端新站，本地与记录均无", ADD,
                () -> action(null, null, "V1"));
    }

    private static void goal() {
        check("原来的两端基线已不同，不能自动建议覆盖", CONFLICT,
                () -> action(new Baseline("V1", "V2"), "V1", "V3"));
        check("远端删除时列出本地关联路径", new Proposal(DELETE, List.of("A→B", "B→C")),
                () -> SYNC.propose(AGREED, "V1", null, true, List.of("A→B", "B→C")));
        check("同名歧义优先阻止，即使值相同", BLOCKED,
                () -> SYNC.propose(AGREED, "V1", "V1", false, List.of()).action());
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
