package cn.shiinapple.gateway.bridge;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 只保留网关的参数绑定和调用编排，传输层由适配接口模拟。 */
final class HttpRpcGateway {
    record Request(String method, String path, Map<String, String> query) {
        Request {
            Objects.requireNonNull(method);
            Objects.requireNonNull(path);
            query = Map.copyOf(query);
        }
    }

    record Response(int status, String body) {}
    record Invocation(String service, String method, List<String> types, List<Object> arguments) {}
    record RouteKey(String method, String path) {}
    record Route(String service, String method, List<Parameter> parameters) {
        Route {
            parameters = List.copyOf(parameters);
        }
    }

    enum Type {
        STRING("java.lang.String"), POSITIVE_INT("int");

        private final String rpcName;

        Type(String rpcName) {
            this.rpcName = rpcName;
        }

        Object parse(String raw) {
            if (this == STRING) return raw;
            int value = Integer.parseInt(raw);
            if (value <= 0) throw new IllegalArgumentException("参数必须大于 0");
            return value;
        }
    }

    record Parameter(String name, Type type) {}

    @FunctionalInterface
    interface Invoker {
        String invoke(Invocation call) throws UpstreamUnavailable;
    }

    static final class UpstreamUnavailable extends Exception {
        UpstreamUnavailable(String message) {
            super(message);
        }
    }

    private final Map<RouteKey, Route> routes;
    private final Invoker invoker;

    HttpRpcGateway(Map<RouteKey, Route> routes, Invoker invoker) {
        this.routes = Map.copyOf(routes);
        this.invoker = Objects.requireNonNull(invoker);
    }

    Response handle(Request request) {
        // 只从登记过的映射确定服务与方法。查询参数仅用来绑定形参。
        Route route = routes.get(new RouteKey(request.method(), request.path()));
        if (route == null) return new Response(404, "route not found");

        List<String> types = route.parameters().stream().map(p -> p.type().rpcName).toList();
        List<Object> arguments;
        try {
            arguments = route.parameters().stream().map(p -> bind(p, request.query())).toList();
        } catch (IllegalArgumentException invalidInput) {
            return new Response(400, "invalid argument");
        }

        try {
            var invocation = new Invocation(route.service(), route.method(), types, arguments);
            return new Response(200, invoker.invoke(invocation));
        } catch (UpstreamUnavailable failure) {
            return new Response(502, "upstream unavailable");
        }
    }

    private static Object bind(Parameter parameter, Map<String, String> query) {
        String raw = query.get(parameter.name());
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("缺少参数");
        return parameter.type().parse(raw);
    }
}
