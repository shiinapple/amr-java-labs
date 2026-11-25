# 14 · 一条 HTTP 请求怎么变成一次 RPC 调用？

对应简历 API 网关项目里的**请求处理和泛化调用**。Java 示例使用内存里的请求对象和假下游，便于跟着数据走。

## 先看一个具体问题

浏览器提交 `GET /api/orders?limit=2&customer=C001`。服务端提供的方法叫 `OrderService.find(String customer, int limit)`。网关怎样确定服务和方法，又怎样把查询参数转成方法需要的顺序和类型？

### 通俗解释

把网关当成接线员。用户给出地址和查询条件，接线员先在通讯录里找到允许接通的部门，再把用户提供的信息按部门的表格顺序填好。通讯录由服务配置提供。表格填不全时，接线员直接要求重填。

### 正式概念

- **HTTP** 是外部入口。本题用方法、路径和查询参数表示解析后的请求。
- **RPC** 是调用远端服务方法的方式。本题用一个 `Invoker` 接口替代传输层。
- **泛化调用**按服务、方法、参数类型和参数值描述一次调用。Dubbo Java SDK 的示例可以用 `GenericService` 结合服务引用完成这类调用，具体配置取决于部署协议与版本。 [Apache Dubbo Java 泛化调用文档](https://cn.dubbo.apache.org/en/overview/mannual/java-sdk/tasks/framework/generic/)
- **参数绑定**按配置声明的形参顺序取值并转换类型。HTTP 参数的书写顺序可以变化。

```text
Request(method, path, query)
     │  (GET, /api/orders)
     ▼
Route(service, method, parameters)
     │  customer → String，limit → int
     ▼
Invocation(OrderService, find, [String, int], [C001, 2])
     │
     ▼
Invoker → Response
```

## 完整示范

按 [macOS 运行指南](../docs/macos.md) 运行本题。程序会打印 200 响应和捕获到的调用参数。加 `--goal` 再测试参数边界与下游故障。

在 [HttpRpcLab.java](src/cn/shiinapple/gateway/bridge/HttpRpcLab.java) 看入口测试如何构造路由表，再打开 [HttpRpcGateway.java](src/cn/shiinapple/gateway/bridge/HttpRpcGateway.java) 的 `handle`：

1. 根据请求方法和路径查表，得到 `OrderService.find`。
2. 遍历配置里的形参。先查 `customer` 得到字符串 `C001`，再查 `limit`，由 `Integer.parseInt` 得到整数 `2`。
3. 向下游传入类型列表和参数列表。假下游把请求存进测试用的 `calls`，你可以直接观察它拿到了什么。
4. 返回响应。本题约定：没匹配到路由为 404，缺参或类型错误为 400，下游模拟失败为 502。

| 请求变化 | 响应 | 是否调用假下游 |
| --- | --- | --- |
| 两个参数合法 | 200 | 是 |
| 去掉 `limit` | 400 | 否 |
| 把方法换成 `POST` | 404 | 否 |
| 输入合法，假下游抛出 `UpstreamUnavailable` | 502 | 是 |

## 独立练习

1. 登记 `GET /api/customers`，让它调用 `CustomerService.find(String id)`。加入成功和缺少 `id` 两个断言。确认旧订单路由依然通过。
2. 给 `limit` 加业务上限 100。补测试 `1`、`100`、`101`，确认 `101` 返回 400 时 `calls` 数量不增加。思考把上限放在通用整数类型或单个路由参数定义，对第一题有什么影响。

## 自测题

1. 调换查询串中两个参数的排列顺序，RPC 入参顺序会变吗？
2. 为什么注册配置决定具体服务和方法？只把 `service`、`method` 当成普通查询参数会有什么问题？
3. 如果下游调用已经成功但响应丢失，网关可以立刻重试所有请求吗？

## 答案与解析

1. 不会。判断线索在遍历对象：代码遍历的是路由的 `parameters`，查询映射只用来按名称取值。
2. 路由映射定义了哪些能力对外开放。让客户端直接选内部接口会绕过这些边界。还应追问用户身份、路由权限和参数校验分别在哪一层完成。
3. 要看操作语义。查询通常可重试，创建、扣减之类的操作要确认幂等机制和超时处理。超时只能说明网关没拿到结果。可接着做 [18 重复消息](../18-event-idempotency-lab/README.md)。

## 面试怎么讲

先用一个请求说清“HTTP 入口 → 路由 → 参数绑定 → RPC 调用”，再讲请求失败和上游失败怎样区分。这里的 `Invoker` 是课堂替身。谈 Netty、Dubbo、Nginx 的工程细节时，以实际用过的项目代码和联调证据为准。
