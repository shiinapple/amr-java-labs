package cn.shiinapple.gateway.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.shiinapple.gateway.bridge.HttpRpcGateway.*;

public final class HttpRpcLab {
    public static void main(String[] args) {
        List<Invocation> calls = new ArrayList<>();
        var key = new RouteKey("GET", "/api/orders");
        var route = new Route("OrderService", "find", List.of(
                new Parameter("customer", Type.STRING), new Parameter("limit", Type.POSITIVE_INT)));
        var gateway = new HttpRpcGateway(Map.of(key, route), call -> {
            calls.add(call);
            return "orders for " + call.arguments().get(0);
        });

        Response response = gateway.handle(request(Map.of("limit", "2", "customer", "C001")));
        check(response.equals(new Response(200, "orders for C001")), "绑定参数后调用");
        check(calls.equals(List.of(new Invocation("OrderService", "find",
                List.of("java.lang.String", "int"), List.of("C001", 2)))), "形参顺序和类型");
        check(gateway.handle(request(Map.of("customer", "C001"))).status() == 400, "缺少 limit");
        check(gateway.handle(new Request("POST", key.path(), Map.of())).status() == 404, "方法也参与映射");
        check(calls.size() == 1, "输入错误时没有下游调用");
        System.out.println(response);
        System.out.println(calls.get(0));

        if (List.of(args).contains("--goal")) {
            check(gateway.handle(request(Map.of("customer", "C001", "limit", "0"))).status() == 400, "拒绝零");
            check(gateway.handle(request(Map.of("customer", "C001", "limit", "2147483648"))).status() == 400,
                    "拒绝超出 int 的参数");
            check(calls.size() == 1, "参数失败仍未调用下游");
            var unavailable = new HttpRpcGateway(Map.of(key, route), call -> {
                throw new UpstreamUnavailable("simulation");
            });
            check(unavailable.handle(request(Map.of("customer", "C001", "limit", "1"))).status() == 502,
                    "下游错误走独立分支");
            System.out.println("通过：参数边界与上游故障");
        }
    }

    private static Request request(Map<String, String> query) {
        return new Request("GET", "/api/orders", query);
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
