package cn.shiinapple.amr.codetracing;

import java.util.List;
import java.util.Optional;

public final class MyCallTrace {
    record Event(String requestId, String caller, String method,
                 String input, String output, boolean failed) {}

    record Report(List<Event> chain, Optional<Event> firstFailure) {}

    Report inspect(List<Event> events, String requestId) {
        // 起步实现认为采样的日志只有一个请求。
        List<Event> chain = List.copyOf(events);
        Optional<Event> failure = chain.stream().reduce((earlier, later) -> later)
                .filter(Event::failed);
        return new Report(chain, failure);
    }
}
