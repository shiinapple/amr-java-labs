package lab;

import java.util.List;

/** 检查结果以及本次调用精确判断的次数。 */
public record CheckResult(List<Conflict> conflicts, long exactChecks) {
    public CheckResult {
        conflicts = List.copyOf(conflicts);
    }
}
