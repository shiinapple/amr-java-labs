package cn.shiinapple.gateway.mcp;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import cn.shiinapple.gateway.mcp.ToolGateway.Error;
import cn.shiinapple.gateway.mcp.ToolGateway.Principal;
import cn.shiinapple.gateway.mcp.ToolGateway.Tool;
import cn.shiinapple.gateway.mcp.ToolGateway.ToolSpec;

public final class McpSessionLab {
    public static void main(String[] args) {
        var alice = new Principal("alpha", "alice");
        var bob = new Principal("beta", "bob");
        var executions = new AtomicInteger();
        var gateway = new ToolGateway(List.of(
                new Tool("format_name", "name", name -> {
                    executions.incrementAndGet();
                    return "Hello " + name.strip();
                }),
                new Tool("number", "text", text -> Integer.toString(Integer.parseInt(text)))),
                Map.of("alpha", Set.of("format_name"), "beta", Set.of("number")));

        var session = gateway.initialize(alice);
        check(session.call(alice, "format_name", Map.of("name", "C001")).error() == Error.NOT_READY,
                "等待 initialized");
        check(session.initialized(alice), "进入可用状态");
        var listed = session.list(alice).orElseThrow();
        check(listed.equals(List.of(new ToolSpec("format_name", "name"))), "只返回有权限的工具");
        var result = session.call(alice, "format_name", Map.of("name", "C001"));
        check(result.value().orElseThrow().equals("Hello C001"), "调用工具");
        System.out.println("tools/list " + listed);
        System.out.println("tools/call " + result.value().orElseThrow());

        if (List.of(args).contains("--goal")) {
            check(session.call(bob, "format_name", Map.of("name", "C002")).error() == Error.UNKNOWN_SESSION,
                    "拒绝跨租户使用会话");
            var colleague = new Principal("alpha", "other");
            check(session.list(colleague).isEmpty(), "同租户不同主体不能读取他人会话");
            check(session.call(alice, "number", Map.of("text", "123")).error() == Error.FORBIDDEN,
                    "直接 call 仍然校验权限");
            check(session.call(alice, "format_name", Map.of("name", " ")).error() == Error.INVALID_ARGUMENTS,
                    "空白参数错误");
            check(session.close(alice), "关闭会话");
            check(session.call(alice, "format_name", Map.of("name", "C001")).error() == Error.UNKNOWN_SESSION,
                    "关闭后不可再调用");
            check(executions.get() == 1, "非法调用没有执行");
            var bobSession = gateway.initialize(bob);
            check(bobSession.initialized(bob), "独立会话");
            check(bobSession.call(bob, "number", Map.of("text", "abc")).error() == Error.TOOL_ERROR,
                    "业务执行错误和请求参数结构错误可区分");
            System.out.println("通过：跨会话隔离、参数错误、工具错误和会话关闭");
        }
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
