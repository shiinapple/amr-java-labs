package lab;

import java.util.ArrayList;
import java.util.List;

/** 固定的旧版参照：不要修改，用来和你的实现比较。 */
public final class ReferencePathChecker implements PathChecker {
    @Override
    public CheckResult check(List<MapPath> paths) {
        List<Conflict> conflicts = new ArrayList<>();
        long exactChecks = 0;
        for (int i = 0; i < paths.size(); i++) {
            for (int j = i + 1; j < paths.size(); j++) {
                exactChecks++;
                if (Geometry.conflicts(paths.get(i), paths.get(j))) {
                    conflicts.add(new Conflict(paths.get(i).id(), paths.get(j).id()));
                }
            }
        }
        return new CheckResult(conflicts, exactChecks);
    }
}
