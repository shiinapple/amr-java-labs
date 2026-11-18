package lab;

/**
 * 已准备好的精确判断，不需要为完成候选筛选而修改。
 * 两条线段相交或最近距离不大于阈值时，认为这对路径有问题。
 * 这是教学简化规则，和项目里的共享站点规则、采样曲线不完全相同。
 */
public final class Geometry {
    public static final double TOLERANCE_METERS = 0.03;
    private static final double EPS = 1e-12;

    private Geometry() {}

    public static boolean conflicts(MapPath a, MapPath b) {
        Point p = a.from(), q = a.to(), r = b.from(), s = b.to();
        if (segmentsIntersect(p, q, r, s)) return true;
        double distance = Math.min(
                Math.min(distanceToSegment(p, r, s), distanceToSegment(q, r, s)),
                Math.min(distanceToSegment(r, p, q), distanceToSegment(s, p, q))
        );
        return distance <= TOLERANCE_METERS;
    }

    private static double cross(Point a, Point b, Point p) {
        return (b.x() - a.x()) * (p.y() - a.y()) - (b.y() - a.y()) * (p.x() - a.x());
    }

    private static boolean onSegment(Point a, Point b, Point p) {
        return Math.abs(cross(a, b, p)) <= EPS
                && p.x() >= Math.min(a.x(), b.x()) - EPS && p.x() <= Math.max(a.x(), b.x()) + EPS
                && p.y() >= Math.min(a.y(), b.y()) - EPS && p.y() <= Math.max(a.y(), b.y()) + EPS;
    }

    private static boolean segmentsIntersect(Point a, Point b, Point c, Point d) {
        double abC = cross(a, b, c), abD = cross(a, b, d);
        double cdA = cross(c, d, a), cdB = cross(c, d, b);
        if (((abC > EPS && abD < -EPS) || (abC < -EPS && abD > EPS))
                && ((cdA > EPS && cdB < -EPS) || (cdA < -EPS && cdB > EPS))) return true;
        return onSegment(a, b, c) || onSegment(a, b, d)
                || onSegment(c, d, a) || onSegment(c, d, b);
    }

    private static double distanceToSegment(Point p, Point a, Point b) {
        double dx = b.x() - a.x(), dy = b.y() - a.y();
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared <= EPS) return Math.hypot(p.x() - a.x(), p.y() - a.y());
        double t = ((p.x() - a.x()) * dx + (p.y() - a.y()) * dy) / lengthSquared;
        t = Math.max(0.0, Math.min(1.0, t));
        return Math.hypot(p.x() - (a.x() + t * dx), p.y() - (a.y() + t * dy));
    }
}
