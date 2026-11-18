package lab;

import java.util.ArrayList;
import java.util.List;

/**
 * 你的主要工作区。
 * 初始版本刻意保留旧项目的两个 for。
 * 目标：不改变冲突结果及顺序，减少 Geometry.conflicts 的真实调用次数。
 */
public final class MyPathChecker implements PathChecker {
    @Override
    public CheckResult check(List<MapPath> paths) {
        List<Conflict> conflicts = new ArrayList<>();
        long exactChecks = 0;

        // TODO: 从这里开始优化。可新增自己的辅助类或方法。
        // 目前程序是for两次的遍历方式，我需要把遍历中不可能冲突的path先排除后再进行遍历，减少时间复杂度
        // 设计逻辑，一条path有四个属性xmin，xmax，ymin，ymax，记录为在xy象限的投影范围
        // 先构建四个二维数组[n,xnmin],[n,ynmin]..按照第二位顺序排序，for(1->n)=>二分查找找到可能冲突的站点，记录为二维数组
        // 二维数组用双层循环再进行真实冲突站点
        // 若不可能
        for (int i = 0; i < paths.size(); i++) {
            for (int j = i + 1; j < paths.size(); j++) {
                MapPath first = paths.get(i);
                MapPath second = paths.get(j);

                // 注意：这里必须统计 Geometry.conflicts 的实际调用次数。
                exactChecks++;
                if (Geometry.conflicts(first, second)) {
                    conflicts.add(new Conflict(first.id(), second.id()));
                }
            }
        }

        return new CheckResult(conflicts, exactChecks);
    }
}
