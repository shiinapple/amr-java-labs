package cn.shiinapple.amr.mapalignment;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import cn.shiinapple.amr.mapalignment.MyFrameAlignment.*;

public final class FrameAlignmentTest {
    private static final MyFrameAlignment SOLVER = new MyFrameAlignment();
    private static final Pair ORIGIN = pair(0, 0, 2, -1);
    private static final Pair RIGHT = pair(4, 0, 2, 3);

    private static Pair pair(double x, double y, double qx, double qy) {
        return new Pair(new Point(x, y), new Point(qx, qy));
    }

    private static List<Long> rounded(Transform transform) {
        return List.of(Math.round(Math.toDegrees(transform.radians()) * 1000),
                Math.round(transform.dx() * 1000), Math.round(transform.dy() * 1000),
                Math.round(transform.rmse() * 1000));
    }

    private static boolean rejects(List<Pair> pairs) {
        try {
            SOLVER.solve(pairs);
            return false;
        } catch (IllegalArgumentException expected) {
            return true;
        }
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
                throw new AssertionError("进阶目标尚未达成，请编辑 MyFrameAlignment.java。");
            }
        } else {
            System.out.println("用 bash RunLab.command --goal 检查进阶目标。");
        }
    }

    private static void baseline() {
        check("90°、2 m、−1 m、零残差（结果乘 1000）", List.of(90000L, 2000L, -1000L, 0L),
                () -> rounded(SOLVER.solve(List.of(ORIGIN, RIGHT))));
        check("负角度也能正确换算", List.of(-90000L, 0L, 0L, 0L),
                () -> rounded(SOLVER.solve(List.of(pair(0, 0, 0, 0), pair(1, 0, 0, -1)))));
        check("一组点无法确定旋转", true,
                () -> rejects(List.of(ORIGIN)));
    }

    private static void goal() {
        check("四点对称偏差由整体拟合摊开（结果乘 1000）", List.of(90000L, 2000L, -1000L, 200L),
                () -> rounded(SOLVER.solve(List.of(pair(-1, -1, 3, -2.2), pair(1, -1, 3, 0.2), pair(-1, 1, 1, -2.2), pair(1, 1, 1, 0.2)))));
        check("前两组重合，第三组还能给出方向", List.of(90000L, 2000L, -1000L, 0L),
                () -> rounded(SOLVER.solve(List.of(ORIGIN, ORIGIN, RIGHT))));
        check("来源点完全重合时无法求解", true,
                () -> rejects(List.of(ORIGIN, ORIGIN, ORIGIN)));
        check("目标点全部坍缩时无法确定方向", true,
                () -> rejects(List.of(ORIGIN, pair(1, 0, 2, -1), pair(0, 1, 2, -1))));
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
