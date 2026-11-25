package lab;

import java.util.ArrayList;
import java.util.List;

/**
 * 不需要 JUnit 的入门测试：直接运行 main。
 * 默认只检查正确性，初始版本也应该通过。
 * 加 --goal 时还检查稀疏地图上的优化目标，初始版本应当失败。
 */
public final class PathCheckerTest {
    private static final PathChecker REFERENCE = new ReferencePathChecker();
    private static final PathChecker MINE = new MyPathChecker();
    private static int cases = 0;

    public static void main(String[] args) {
        expectKnown("空地图", List.of(), List.of());
        expectKnown("只有一条路径", List.of(line("A", 0, 0, 1, 0)), List.of());
        expectKnown("两条线交叉", List.of(line("A", 0, 0, 2, 2), line("B", 0, 2, 2, 0)),
                List.of(new Conflict("A", "B")));
        expectKnown("两条线远离", List.of(line("A", 0, 0, 1, 0), line("B", 5, 0, 6, 0)), List.of());
        expectKnown("距离 2 厘米仍要保留", List.of(line("A", 0, 0, 2, 0), line("B", 0, 0.02, 2, 0.02)),
                List.of(new Conflict("A", "B")));
        expectKnown("距离刚好 3 厘米", List.of(line("A", 0, 0, 2, 0), line("B", 0, 0.03, 2, 0.03)),
                List.of(new Conflict("A", "B")));
        expectKnown("距离超过 3 厘米", List.of(line("A", 0, 0, 2, 0), line("B", 0, 0.031, 2, 0.031)), List.of());
        expectKnown("两个端点重合", List.of(line("A", 0, 0, 1, 0), line("B", 1, 0, 1, 1)),
                List.of(new Conflict("A", "B")));
        expectKnown("点和线段", List.of(line("A", 1, 0, 1, 0), line("B", 0, 0, 2, 0)),
                List.of(new Conflict("A", "B")));
        expectKnown("两条斜线的盒子重叠但线段平行", List.of(line("A", 0, 0, 10, 10), line("B", 0, 5, 5, 10)), List.of());
        expectKnown("文档里的五条线", ExampleMaps.teachingExample(),
                List.of(new Conflict("A", "B"), new Conflict("A", "E"), new Conflict("B", "E")));
        for (int seed = 0; seed < 50; seed++) compare("随机地图 seed=" + seed, ExampleMaps.random(80, seed));
        List<MapPath> sparse = ExampleMaps.sparse(1000);
        compare("1000 条分散路径", sparse);

        CheckResult baseline = REFERENCE.check(sparse);
        CheckResult mine = MINE.check(sparse);
        System.out.printf("正确性通过：%d 个用例。%n", cases);
        System.out.printf("分散地图：旧版精确判断 %,d 次，你的实现 %,d 次。%n", baseline.exactChecks(), mine.exactChecks());
        if (List.of(args).contains("--goal")) {
            if (mine.exactChecks() >= baseline.exactChecks() / 4) {
                throw new AssertionError("优化目标尚未达到：请在不改变结果的前提下，将精确判断次数降至旧版 1/4 以下。");
            }
            System.out.println("优化目标通过：精确判断次数低于旧版的 1/4。");
        } else {
            System.out.println("想检查优化目标时，加参数 --goal。初始双重循环不应通过该目标。");
        }
    }

    private static MapPath line(String id, double x1, double y1, double x2, double y2) {
        return ExampleMaps.line(id, x1, y1, x2, y2);
    }

    private static void expectKnown(String name, List<MapPath> paths, List<Conflict> expected) {
        List<Conflict> baseline = REFERENCE.check(paths).conflicts();
        if (!baseline.equals(expected)) throw new AssertionError(name + "：参照结果与预期不符：" + baseline);
        compare(name, paths);
    }

    private static void compare(String name, List<MapPath> input) {
        List<MapPath> paths = List.copyOf(input);
        List<MapPath> before = new ArrayList<>(paths);
        CheckResult expected = REFERENCE.check(paths);
        CheckResult actual = MINE.check(paths);
        if (!paths.equals(before)) throw new AssertionError(name + "：不能修改输入路径列表");
        if (!expected.conflicts().equals(actual.conflicts())) {
            throw new AssertionError(name + "：结果内容或顺序不一致。\n预期：" + expected.conflicts() + "\n实际：" + actual.conflicts());
        }
        cases++;
    }
}
