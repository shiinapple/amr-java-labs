package cn.shiinapple.gateway.limit;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class RateLimitLab {
    public static void main(String[] args) throws InterruptedException {
        var clock = new AtomicLong();
        var bucket = new TokenBucket(2, 1_000, clock::get);
        check(bucket.tryAcquire().remaining() == 1, "第一次可放行");
        check(bucket.tryAcquire().remaining() == 0, "第二次耗尽");
        clock.set(250);
        var denied = bucket.tryAcquire();
        check(!denied.allowed() && denied.retryAfterMillis() == 750, "拒绝且还需等待 750ms");
        clock.set(1_000);
        check(bucket.tryAcquire().allowed(), "达到周期后补充");
        System.out.println("t=250ms 拒绝，等待 " + denied.retryAfterMillis() + "ms");

        if (List.of(args).contains("--goal")) {
            clock.set(1_999);
            check(bucket.tryAcquire().retryAfterMillis() == 1, "剩余一毫秒");
            clock.set(8_200);
            check(bucket.tryAcquire().remaining() == 1, "长时间空闲也受容量上限约束");
            check(bucket.tryAcquire().allowed(), "消费最后余额");
            check(bucket.tryAcquire().retryAfterMillis() == 800, "保留不足一周期的余数");
            try {
                clock.set(8_100);
                bucket.tryAcquire();
                throw new AssertionError("时钟倒退应报错");
            } catch (IllegalStateException expected) {
                clock.set(9_000);
            }
            check(bucket.tryAcquire().allowed(), "无效时钟不改变桶状态");
            raceOneToken();
            System.out.println("通过：容量上限、时钟边界和并发抢令牌");
        }
    }

    private static void raceOneToken() throws InterruptedException {
        var single = new TokenBucket(1, 1_000, () -> 0);
        var passed = new AtomicInteger();
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        Runnable attempt = () -> {
            ready.countDown();
            try {
                start.await();
                if (single.tryAcquire().allowed()) passed.incrementAndGet();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        };
        Thread first = new Thread(attempt);
        Thread second = new Thread(attempt);
        first.start();
        second.start();
        ready.await();
        start.countDown();
        first.join();
        second.join();
        check(passed.get() == 1, "一枚令牌只能放行一次");
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
