package cn.shiinapple.amr.mapalignment;

import java.util.List;

public final class MyFrameAlignment {
    record Point(double x, double y) {}
    record Pair(Point source, Point target) {}

    record Transform(double radians, double dx, double dy, double rmse) {
        Point apply(Point p) {
            return new Point(Math.cos(radians) * p.x() - Math.sin(radians) * p.y() + dx,
                    Math.sin(radians) * p.x() + Math.cos(radians) * p.y() + dy);
        }
    }

    Transform solve(List<Pair> pairs) {
        if (pairs.size() < 2) {
            throw new IllegalArgumentException("至少需要两组对应点");
        }
        // 起步算法只用前两组点决定变换。
        Pair first = pairs.get(0);
        Pair second = pairs.get(1);
        double px = second.source().x() - first.source().x();
        double py = second.source().y() - first.source().y();
        double qx = second.target().x() - first.target().x();
        double qy = second.target().y() - first.target().y();
        if (Math.hypot(px, py) < 1e-9 || Math.hypot(qx, qy) < 1e-9) {
            throw new IllegalArgumentException("无法从这两个点确定旋转");
        }

        double radians = Math.atan2(px * qy - py * qx, px * qx + py * qy);
        Point rotated = new Transform(radians, 0, 0, 0).apply(first.source());
        double dx = first.target().x() - rotated.x();
        double dy = first.target().y() - rotated.y();
        Transform transform = new Transform(radians, dx, dy, 0);
        double squaredError = 0;
        for (Pair pair : pairs) {
            Point predicted = transform.apply(pair.source());
            double errX = predicted.x() - pair.target().x();
            double errY = predicted.y() - pair.target().y();
            squaredError += errX * errX + errY * errY;
        }
        return new Transform(radians, dx, dy, Math.sqrt(squaredError / pairs.size()));
    }
}
