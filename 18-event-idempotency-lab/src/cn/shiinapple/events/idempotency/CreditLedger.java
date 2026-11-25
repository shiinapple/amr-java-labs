package cn.shiinapple.events.idempotency;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** 使用虚构积分演示消费幂等。全部状态由同一个进程内临界区保护。 */
final class CreditLedger {
    record Event(String source, String eventId, String account, long amount) {
        Event {
            Objects.requireNonNull(source);
            Objects.requireNonNull(eventId);
            Objects.requireNonNull(account);
            if (source.isBlank() || eventId.isBlank() || account.isBlank() || amount <= 0) {
                throw new IllegalArgumentException("事件来源、编号、账户必填，积分必须大于零");
            }
        }
    }

    enum Outcome { APPLIED, DUPLICATE, CONFLICT, OVERFLOW }
    private record EventKey(String source, String eventId) {}

    private final Map<EventKey, Event> processed = new HashMap<>();
    private final Map<String, Long> balances = new HashMap<>();

    synchronized Outcome apply(Event event) {
        var key = new EventKey(event.source(), event.eventId());
        Event previous = processed.get(key);
        if (previous != null) {
            // 同编号但不同内容应进入异常处理，不能当作正常重放。
            return previous.equals(event) ? Outcome.DUPLICATE : Outcome.CONFLICT;
        }

        long next;
        try {
            next = Math.addExact(balance(event.account()), event.amount());
        } catch (ArithmeticException overflow) {
            // 先做全部校验，未通过时余额和消费记录都不变。
            return Outcome.OVERFLOW;
        }
        balances.put(event.account(), next);
        processed.put(key, event);
        return Outcome.APPLIED;
    }

    synchronized long balance(String account) {
        return balances.getOrDefault(account, 0L);
    }
}
