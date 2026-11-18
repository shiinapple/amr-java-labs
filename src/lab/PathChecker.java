package lab;

import java.util.List;

/** 一个地图检查器接收路径列表，返回结果。 */
public interface PathChecker {
    CheckResult check(List<MapPath> paths);
}
