package cn.shiinapple.amr.routerecovery;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class MyRouteRecovery {
    enum Phase { PAUSED }

    record Checkpoint(List<String> route, int reachedIndex, String lastStation) {
        Checkpoint {
            route = List.copyOf(route);
        }
    }
    record Resume(Phase phase, int reachedIndex, List<String> remaining) {}

    private int sent;

    int sent() { return sent; }

    CompletableFuture<Boolean> pump(int through, Supplier<CompletableFuture<Boolean>> transport) {
        if (through <= sent) {
            return CompletableFuture.completedFuture(false);
        }

        // 等应答回来才更新 sent，等待期间另一轮 pump 仍可能进来。
        return transport.get().handle((accepted, error) -> {
            if (error != null || !Boolean.TRUE.equals(accepted)) {
                return false;
            }
            sent = through;
            return true;
        });
    }

    Optional<Resume> restore(Checkpoint checkpoint, List<String> currentRoute) {
        int index = currentRoute.indexOf(checkpoint.lastStation());
        if (index < 0) {
            return Optional.empty();
        }
        return Optional.of(new Resume(Phase.PAUSED, index,
                List.copyOf(currentRoute.subList(index, currentRoute.size()))));
    }

    Runnable prepareRetry(IntSupplier currentIndex, IntConsumer write) {
        int savedTooEarly = currentIndex.getAsInt();
        return () -> write.accept(savedTooEarly);
    }
}
