# 17 · 能看到一个工具，就一定能调用它吗？

旧版简历里的 MCP 网关作为拓展。本题使用两位租户的会话和两个进程内字符串工具，练习初始化和授权边界。

## 先看一个具体问题

租户 alpha 可以使用名字格式化工具，租户 beta 可以使用数字解析工具。alpha 已经初始化并拿到工具列表。如果它直接发来 `tools/call` 调用 `number`，网关该看哪份信息？beta 能拿 alpha 的会话去调用吗？

### 通俗解释

进入图书馆后要先完成登记。书单只展示你能借的书。借书时仍要检查证件和书的权限，既要确认持证人是这个会话的拥有者，也要确认读者能借这本书。

### 正式概念

- **初始化**用于交换协议版本和能力。约定版本为 2025-11-25 的规范中，客户端成功初始化后还发送 `notifications/initialized`。本题用 Java 方法表示两步状态转换。 [MCP 生命周期规范](https://modelcontextprotocol.io/specification/2025-11-25/basic/lifecycle)
- **工具发现**由 `tools/list` 取得。**工具调用**用 `tools/call` 指定名称和参数。本例把工具描述压缩为名称和唯一必填的字符串参数。 [MCP 工具规范](https://modelcontextprotocol.io/specification/2025-11-25/server/tools)
- **身份**在本题由外层传入，经会话比较租户和主体。实际入口须通过可信认证流程取得身份。
- **权限**回答该身份能用哪个工具。本题按租户配置许可，会话归属再细到主体。

```text
initialize → INITIALIZED → notifications/initialized → READY
                                                     ├─ list → 仅列可用工具
                                                     ├─ call → 核对归属、权限与参数
                                                     └─ close → CLOSED
```

## 完整示范

按 [macOS 运行指南](../docs/macos.md) 运行本题。基础示例打印工具列表和 `Hello C001`。`--goal` 覆盖跨租户、同租户不同主体、直接调用未授权工具、参数错误和会话关闭。

从 [McpSessionLab.java](src/cn/shiinapple/gateway/mcp/McpSessionLab.java) 的 `main` 顺着事件读。再到 [ToolGateway.java](src/cn/shiinapple/gateway/mcp/ToolGateway.java) 看静态工具表、租户许可和会话状态如何分工。

| 操作 | 结果 |
| --- | --- |
| alpha 在会话就绪前调用 `format_name` | `NOT_READY` |
| alpha 完成两步初始化后列出工具 | 只有 `format_name` |
| alpha 调用 `format_name`，参数为 `name=C001` | `Hello C001` |
| alpha 直接调用 `number` | `FORBIDDEN` |
| beta 使用 alpha 的会话 | `UNKNOWN_SESSION` |
| beta 在自己的会话中传 `text=abc` 调用 `number` | `TOOL_ERROR` |

本例的请求、工具参数和结果都在 Java 进程内。真正的 MCP 接入还需要 JSON-RPC 消息编解码、协议协商和对应传输。

## 独立练习

1. 增加 `count_chars`，用字符串长度作为结果，只给 beta 授权。测试 beta 能列出和调用，alpha 直接调用被拒绝。用 ASCII 样例 `abc` 预期返回 `3`，再思考 Unicode 中“字符”可以有哪些定义。
2. 给会话增加可控的到期时间。到期后 list 和 call 都应拒绝。测试到期前 1ms、刚到期、到期后 1ms。编码前先定义创建和延期规则。

## 自测题

1. 客户端以前拿过工具列表，之后是否可以只凭这份列表调用工具？
2. 为什么会话归属要同时比较租户和主体？
3. 请求缺少参数，与工具执行时发现内容无法解析，定位问题时分别看哪一层？

## 答案与解析

1. 不能。列表是一次查询的结果，调用时仍要按当前权限与归属核对。只查列表会漏掉直接调用和权限变化。
2. 不同租户的主体编号可能相同。同租户内也可能有多个主体。判断线索是“这位调用者是不是会话创建者”，仅有主体或租户都不足。
3. 缺少参数属于网关收到的请求结构有问题。结构正确但字符串无法解析，是工具自己的执行结果。本例通过 `INVALID_ARGUMENTS` 和 `TOOL_ERROR` 区分。

## 面试怎么讲

按初始化、工具发现、授权调用拆开讲。若被问到 SSE、会话标识或动态注册，先说清自己项目用的是哪种传输、身份如何传递、配置变更对已有会话怎样生效，再补充这道练习验证到的范围。
