package cn.shiinapple.gateway.limit;

import java.util.Objects;
import java.util.function.LongSupplier;

/** 单实例、固定时间刻度的令牌桶。每经过一个时间间隔补一个令牌。 */
final class TokenBucket {
    record Decision(boolean allowed, int remaining, long retryAfterMillis) {}

    private final int capacity;
    private final long intervalMillis;
    private final LongSupplier clockMillis;
    private int available;
    private long lastRefillMillis;
    private long lastObservedMillis;

    TokenBucket(int capacity, long intervalMillis, LongSupplier clockMillis) {
        if (capacity <= 0 || intervalMillis <= 0) {
            throw new IllegalArgumentException("容量与补充间隔必须为正");
        }
        this.capacity = capacity;
        this.intervalMillis = intervalMillis;
        this.clockMillis = Objects.requireNonNull(clockMillis);
        this.available = capacity;
        this.lastRefillMillis = clockMillis.getAsLong();
        if (lastRefillMillis < 0) throw new IllegalArgumentException("时钟必须返回非负值");
        this.lastObservedMillis = lastRefillMillis;
    }

    synchronized Decision tryAcquire() {
        // 补充和扣减在同一个临界区内，否则两个请求可能拿走同一个令牌。
        long now = clockMillis.getAsLong();
        if (now < lastObservedMillis) throw new IllegalStateException("时钟发生倒退");
        lastObservedMillis = now;
        long elapsed = now - lastRefillMillis;
        long earned = elapsed / intervalMillis;
        available += (int) Math.min(earned, capacity - available);
        // 保留未满一个周期的余数，下一次调用还能继续累计这段时间。
        lastRefillMillis = now - elapsed % intervalMillis;

        if (available == 0) {
            return new Decision(false, 0, intervalMillis - (now - lastRefillMillis));
        }
        return new Decision(true, --available, 0);
    }
}
