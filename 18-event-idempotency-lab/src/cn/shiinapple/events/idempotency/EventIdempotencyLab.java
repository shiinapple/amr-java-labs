package cn.shiinapple.events.idempotency;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.shiinapple.events.idempotency.CreditLedger.*;

public final class EventIdempotencyLab {
    public static void main(String[] args) throws InterruptedException {
        var ledger = new CreditLedger();
        var event = new Event("course", "evt-1", "A", 30);
        check(ledger.apply(event) == Outcome.APPLIED, "首次入账");
        check(ledger.apply(event) == Outcome.DUPLICATE, "重放已处理事件");
        check(ledger.apply(new Event("course", "evt-1", "A", 90)) == Outcome.CONFLICT,
                "同编号的内容发生变化");
        check(ledger.balance("A") == 30, "重复投递和冲突不改余额");
        System.out.println("evt-1 投递两次，账户 A 积分 " + ledger.balance("A"));

        if (List.of(args).contains("--goal")) {
            check(ledger.apply(new Event("attendance", "evt-1", "A", 5)) == Outcome.APPLIED,
                    "不同来源的事件编号可相同");
            check(ledger.apply(new Event("course", "evt-2", "A", Long.MAX_VALUE)) == Outcome.OVERFLOW,
                    "积分溢出后拒绝提交");
            check(ledger.apply(new Event("course", "evt-2", "A", 10)) == Outcome.APPLIED,
                    "操作失败后没有留下去重记录");
            check(ledger.balance("A") == 45, "所有已接受事件的正确合计");
            raceDuplicate();
            System.out.println("通过：来源隔离、溢出和并发重放");
        }
    }

    private static void raceDuplicate() throws InterruptedException {
        var ledger = new CreditLedger();
        var event = new Event("course", "evt-2", "B", 15);
        var applied = new AtomicInteger();
        var duplicates = new AtomicInteger();
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        Runnable consume = () -> {
            ready.countDown();
            try {
                start.await();
                Outcome outcome = ledger.apply(event);
                if (outcome == Outcome.APPLIED) applied.incrementAndGet();
                if (outcome == Outcome.DUPLICATE) duplicates.incrementAndGet();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        };
        Thread first = new Thread(consume);
        Thread second = new Thread(consume);
        first.start();
        second.start();
        ready.await();
        start.countDown();
        first.join();
        second.join();
        check(applied.get() == 1 && duplicates.get() == 1 && ledger.balance("B") == 15,
                "同事件并发投递只入账一次");
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
