package cn.shiinapple.gateway.mcp;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 进程内会话模型。请求里的身份由假定可信的调用层提供。 */
final class ToolGateway {
    record Principal(String tenant, String subject) {}
    record Tool(String name, String requiredArgument, Function<String, String> execute) {}
    record ToolSpec(String name, String requiredArgument) {}
    enum Error { NONE, UNKNOWN_SESSION, FORBIDDEN, NOT_READY, INVALID_ARGUMENTS, TOOL_ERROR }
    record Result(Optional<String> value, Error error) {
        static Result failure(Error error) {
            return new Result(Optional.empty(), error);
        }

        static Result success(String value) {
            return new Result(Optional.of(value), Error.NONE);
        }
    }

    private final Map<String, Tool> toolsByName;
    private final Map<String, Set<String>> allowedByTenant;

    ToolGateway(List<Tool> tools, Map<String, Set<String>> allowedByTenant) {
        // toUnmodifiableMap 会拒绝同名工具，避免配置覆盖造成权限含义变化。
        this.toolsByName = tools.stream().collect(Collectors.toUnmodifiableMap(
                Tool::name, Function.identity()));
        this.allowedByTenant = allowedByTenant.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
    }

    Session initialize(Principal caller) {
        return new Session(caller);
    }

    final class Session {
        private enum State { INITIALIZED, READY, CLOSED }

        private final Principal owner;
        private State state = State.INITIALIZED;

        private Session(Principal owner) {
            this.owner = Objects.requireNonNull(owner);
        }

        synchronized boolean initialized(Principal caller) {
            if (!owner.equals(caller) || state != State.INITIALIZED) return false;
            state = State.READY;
            return true;
        }

        synchronized Optional<List<ToolSpec>> list(Principal caller) {
            if (!owner.equals(caller) || state != State.READY) return Optional.empty();
            List<ToolSpec> visible = allowed().stream().sorted()
                    .filter(toolsByName::containsKey)
                    .map(toolsByName::get)
                    .map(tool -> new ToolSpec(tool.name(), tool.requiredArgument()))
                    .toList();
            return Optional.of(visible);
        }

        Result call(Principal caller, String name, Map<String, String> arguments) {
            Tool tool;
            synchronized (this) {
                if (!owner.equals(caller) || state == State.CLOSED) {
                    return Result.failure(Error.UNKNOWN_SESSION);
                }
                if (state != State.READY) return Result.failure(Error.NOT_READY);

                // 即使调用方跳过工具列表，call 也必须独立做权限检查。
                if (!allowed().contains(name)) return Result.failure(Error.FORBIDDEN);
                tool = toolsByName.get(name);
                if (tool == null) return Result.failure(Error.FORBIDDEN);
            }

            String argument = arguments.get(tool.requiredArgument());
            if (arguments.size() != 1 || argument == null || argument.isBlank()) {
                return Result.failure(Error.INVALID_ARGUMENTS);
            }

            // 工具运行属于会话锁之外，close 只阻止之后的新调用。
            try {
                return Result.success(tool.execute().apply(argument));
            } catch (IllegalArgumentException failure) {
                return Result.failure(Error.TOOL_ERROR);
            }
        }

        synchronized boolean close(Principal caller) {
            if (!owner.equals(caller) || state == State.CLOSED) return false;
            state = State.CLOSED;
            return true;
        }

        private Set<String> allowed() {
            return allowedByTenant.getOrDefault(owner.tenant(), Set.of());
        }
    }
}
