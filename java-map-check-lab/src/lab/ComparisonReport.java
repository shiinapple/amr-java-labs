package lab;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** 生成离线 HTML，对同一批路径运行真实 Java 检查器。 */
public final class ComparisonReport {
    private record Measurement(CheckResult result, double medianMs) {}

    private static Measurement measure(PathChecker checker, List<MapPath> paths) {
        for (int i = 0; i < 3; i++) checker.check(paths);
        long[] times = new long[7];
        CheckResult result = null;
        for (int i = 0; i < times.length; i++) {
            long start = System.nanoTime();
            result = checker.check(paths);
            times[i] = System.nanoTime() - start;
        }
        Arrays.sort(times);
        return new Measurement(result, times[times.length / 2] / 1_000_000.0);
    }

    private static String section(String id, String title, List<MapPath> paths) {
        Measurement old = measure(new ReferencePathChecker(), paths);
        Measurement mine = measure(new MyPathChecker(), paths);
        boolean same = old.result().conflicts().equals(mine.result().conflicts());
        Set<String> highlighted = new HashSet<>();
        for (Conflict c : mine.result().conflicts()) {
            highlighted.add(c.firstPathId()); highlighted.add(c.secondPathId());
        }
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (MapPath p : paths) for (Point point : List.of(p.from(), p.to())) {
            minX = Math.min(minX, point.x()); minY = Math.min(minY, point.y());
            maxX = Math.max(maxX, point.x()); maxY = Math.max(maxY, point.y());
        }
        double scale = Math.min(940 / Math.max(1, maxX - minX), 460 / Math.max(1, maxY - minY));
        StringBuilder svg = new StringBuilder();
        for (MapPath p : paths) {
            svg.append(String.format(Locale.US, "<line x1='%.2f' y1='%.2f' x2='%.2f' y2='%.2f' class='%s'/>",
                    20 + (p.from().x() - minX) * scale, 480 - (p.from().y() - minY) * scale,
                    20 + (p.to().x() - minX) * scale, 480 - (p.to().y() - minY) * scale,
                    highlighted.contains(p.id()) ? "hit" : "line"));
        }
        return String.format(Locale.US, """
            <section id='%s' hidden><h2>%s · %,d 条路径</h2>
            <p class='%s'>%s</p>
            <div class='cards'><div><b>旧版参照</b><strong>%.3f 毫秒</strong><span>精确判断 %,d 次</span><span>冲突 %,d 对</span></div>
            <div><b>我的实现</b><strong>%.3f 毫秒</strong><span>精确判断 %,d 次</span><span>冲突 %,d 对</span></div></div>
            <p>蓝色为普通路径，红色表示参与冲突。每组两条路径可能非常接近，缩放后看起来像一条。</p>
            <svg viewBox='0 0 980 500' role='img' aria-label='路径分布图'>%s</svg></section>
            """, id, title, paths.size(), same ? "ok" : "bad", same ? "两种实现的冲突结果和顺序一致" : "警告：冲突结果或顺序不一致，请先修正正确性",
                old.medianMs(), old.result().exactChecks(), old.result().conflicts().size(),
                mine.medianMs(), mine.result().exactChecks(), mine.result().conflicts().size(), svg);
    }

    public static void main(String[] args) throws Exception {
        String sparse = section("sparse", "分散路径", ExampleMaps.sparse(1000));
        String random = section("random", "随机交叉", ExampleMaps.random(1000, 42));
        String html = """
            <!doctype html><html lang='zh-CN'><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
            <title>地图路径检查结果</title><style>
            *{box-sizing:border-box}body{font-family:system-ui,sans-serif;background:#f5f7fa;color:#243148;margin:0}main{max-width:1050px;margin:auto;padding:32px 20px}h1{margin-bottom:8px}p{line-height:1.6;color:#526078}button{border:1px solid #b6c5d7;background:white;border-radius:8px;padding:10px 18px;margin:12px 8px 12px 0;cursor:pointer;font-size:15px}button.active{background:#245cab;color:white;border-color:#245cab}.cards{display:flex;gap:16px;flex-wrap:wrap}.cards div{background:white;border-radius:12px;padding:20px;flex:1;min-width:220px;display:grid;gap:8px;box-shadow:0 2px 12px #23344b10}.cards strong{font-size:28px;color:#245cab}.cards span{color:#526078}.ok{color:#16805d}.bad{color:#c13838;font-weight:bold}svg{width:100%%;background:white;border-radius:12px;border:1px solid #dce4ed}.line{stroke:#3466a0;stroke-width:1.5}.hit{stroke:#d43e3e;stroke-width:2}section[hidden]{display:none}
            </style><main><h1>地图路径检查对比</h1><p>同一张地图，直接运行你的 Java 代码和旧版参照。每种实现先预热 3 次，再取 7 次耗时的中位数；耗时会受机器负载、运行顺序等影响。</p>
            <button class='active' onclick="show('sparse',this)">分散路径</button><button onclick="show('random',this)">随机交叉</button>
            %s %s <p>这是本次运行生成的离线结果。修改 Java 代码后，重新双击启动文件可更新结果。</p></main>
            <script>function show(id,button){document.querySelectorAll('section').forEach(s=>s.hidden=s.id!==id);document.querySelectorAll('button').forEach(b=>b.classList.toggle('active',b===button))}show('sparse',document.querySelector('button'))</script></html>
            """.formatted(sparse, random);
        Path output = Path.of("out", "comparison.html");
        Files.createDirectories(output.getParent());
        Files.writeString(output, html, StandardCharsets.UTF_8);
        System.out.println("结果已生成：" + output.toAbsolutePath());
    }
}
